package translationapi.nllbtranslation;

import android.content.Context;
import android.icu.text.BreakIterator;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.moe.moetranslator.R;
import com.moe.moetranslator.translate.CustomLocale;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class TranslationCore {
    public static final int NLLB_CACHE = 6;
    public static final int BEAM_SIZE = 1;
    private final int mode = 6;
    private Tokenizer tokenizer;
    private OrtEnvironment onnxEnv;
    private OrtSession encoderSession;
    private OrtSession decoderSession;
    private OrtSession cacheInitSession;
    private OrtSession embedAndLmHeadSession;
    private OrtSession embedSession;
    private Map<String, String> nllbLanguagesCodes = new HashMap<String, String>();
    private static final double EOS_PENALTY = 0.9;
    private long currentResultID = 0;
    private final Object lock = new Object();
    private final int EMPTY_BATCH_SIZE = 1;
    private boolean translatingMessages = false;
    private boolean translating = false;
    private android.os.Handler mainHandler;   // handler that can be used to post to the main thread
    private InitializationListener initListener;

    public TranslationCore(@NonNull Context ctx, InitializationListener initializationListener) {
        this.initListener = initializationListener;
        mainHandler = new android.os.Handler(Looper.getMainLooper());
        initializeNllbLanguagesCodes(ctx);

        String encoderPath = ctx.getExternalFilesDir(null).getPath() + "/models/NLLB_encoder.onnx";
        String decoderPath = ctx.getExternalFilesDir(null).getPath() + "/models/NLLB_decoder.onnx";
        String vocabPath = ctx.getExternalFilesDir(null).getPath() + "/models/sentencepiece_bpe.model";
        String embedAndLmHeadPath = ctx.getExternalFilesDir(null).getPath() + "/models/NLLB_embed_and_lm_head.onnx";
        String cacheInitializerPath = ctx.getExternalFilesDir(null).getPath() + "/models/NLLB_cache_initializer.onnx";

        final Thread t = new Thread("textTranslation") {
            public void run() {
                onnxEnv = OrtEnvironment.getEnvironment();

                try {
                    OrtSession.SessionOptions decoderOptions = new OrtSession.SessionOptions();
                    decoderOptions.setMemoryPatternOptimization(false);
                    decoderOptions.setCPUArenaAllocator(false);
                    decoderOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT);
                    decoderSession = onnxEnv.createSession(decoderPath, decoderOptions);

                    OrtSession.SessionOptions encoderOptions = new OrtSession.SessionOptions();
                    encoderOptions.setMemoryPatternOptimization(false);
                    encoderOptions.setCPUArenaAllocator(false);
                    encoderOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT);
                    encoderSession = onnxEnv.createSession(encoderPath, encoderOptions);

                    OrtSession.SessionOptions cacheInitOptions = new OrtSession.SessionOptions();
                    cacheInitOptions.setMemoryPatternOptimization(false);
                    cacheInitOptions.setCPUArenaAllocator(false);
                    cacheInitOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT);
                    cacheInitSession = onnxEnv.createSession(cacheInitializerPath, cacheInitOptions);

                    OrtSession.SessionOptions embedAndLmHeadOptions = new OrtSession.SessionOptions();
                    embedAndLmHeadOptions.setMemoryPatternOptimization(false);
                    embedAndLmHeadOptions.setCPUArenaAllocator(false);
                    embedAndLmHeadOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT);

                    embedAndLmHeadSession = onnxEnv.createSession(embedAndLmHeadPath, embedAndLmHeadOptions);

                    decoderOptions.close();
                    encoderOptions.close();
                    cacheInitOptions.close();

                    // 告知初始化完成
                    mainHandler.post(() -> initListener.onInitializationComplete());


                } catch (OrtException e) {
                    e.printStackTrace();

                    // 告知初始化失败
                    mainHandler.post(() -> initListener.onInitializationError(e));

                }

                tokenizer = new Tokenizer(vocabPath, Tokenizer.NLLB);

            }
        };
        t.start();
    }

    private interface TranslatorListener {
        void onFailure(Exception e);
    }

    public interface TranslateListener extends TranslatorListener {
        void onTranslatedText(String text, long resultID, boolean isFinal, CustomLocale languageOfText);
    }

    // 初始化NLLB的语言代码
    private void initializeNllbLanguagesCodes(Context ctx){
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(ctx.getResources().openRawResource(R.raw.nllb_support_languages));
            NodeList listCode = document.getElementsByTagName("code");
            NodeList listCodeNllb = document.getElementsByTagName("code_NLLB");
            for (int i = 0; i < listCode.getLength(); i++) {
                nllbLanguagesCodes.put(listCode.item(i).getTextContent(), listCodeNllb.item(i).getTextContent());
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private String getNllbLanguageCode(String languageCode){
        if(nllbLanguagesCodes != null) {
            String nllbCode = nllbLanguagesCodes.get(languageCode);
            if (nllbCode == null) {
                Log.e("error", "Error Converting Language code " + languageCode + " to NLLB code");
                return languageCode;
            } else {
                return nllbCode;
            }
        }else{
            Log.e("error", "Error Converting Language code " + languageCode + " to NLLB code, the NllbLanguagesCodes are not initialized");
            return languageCode;
        }
    }

    // 参数待翻译文字，源语言，目标语言，集束搜索束宽，是否保存结果
    public void translate(final String textToTranslate, final CustomLocale languageInput, final CustomLocale languageOutput, final TranslationListener listener) {
        // 启动一个新的线程来进行翻译
        final Thread t = new Thread("textTranslation") {
            public void run() {
                try {
                    translating = true;
                    String result = performTextTranslation(textToTranslate, languageInput, languageOutput);
                    if (listener != null) {
                        listener.onTranslationComplete(result);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onTranslationError(e);
                    }
                } finally {
                    translating = false;
                }
            }
        };
        t.start();
    }

    @Nullable
    private OnnxTensor executeEncoder(int[] inputIDs, int[] attentionMask){
        try {
            OnnxTensor inputIDsTensor = TensorUtils.convertIntArrayToTensor(onnxEnv, inputIDs);
            OnnxTensor attentionMaskTensor = TensorUtils.convertIntArrayToTensor(onnxEnv, attentionMask);
            Map<String,OnnxTensor> input = new HashMap<String,OnnxTensor>();
            OrtSession.Result embedResult = null;

            //we do the embedding separately and then we pass the result to the encoder
            Map<String,OnnxTensor> embedInput = new HashMap<String,OnnxTensor>();
            embedInput.put("input_ids", inputIDsTensor);
            embedInput.put("pre_logits", TensorUtils.createFloatTensorWithSingleValue(onnxEnv, 0, new long[]{EMPTY_BATCH_SIZE, 1, 1024}));
            embedInput.put("use_lm_head", TensorUtils.convertBooleanToTensor(onnxEnv, false));
            ArraySet<String> requestedOutputs = new ArraySet<>();
            requestedOutputs.add("embed_matrix");
            embedResult = embedAndLmHeadSession.run(embedInput, requestedOutputs);

            input.put("input_ids",inputIDsTensor);
            input.put("attention_mask",attentionMaskTensor);
            input.put("embed_matrix", (OnnxTensor) embedResult.get(0));

            OrtSession.Result result = encoderSession.run(input);

            embedResult.close();

            Optional<OnnxValue> output = result.get("last_hidden_state");
            //Object value = output.get().getValue();   //utile solo per il debug
            return (OnnxTensor) output.get();
        } catch (OrtException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void executeCacheDecoderGreedy(TokenizerResult input, OnnxTensor encoderResult, ArrayList<Integer> completeOutput, final CustomLocale outputLanguage, final TranslateListener responseListener){
        try {
            long time = System.currentTimeMillis();
            long initialTime;
            final int eos = tokenizer.PieceToID("</s>");
            int nLayers;
            int hiddenSize;

            // mode == NLLB_CACHE
            nLayers = 12;
            hiddenSize = 64;


            int[] input_ids;

            input_ids = new int[]{2};

            OnnxTensor inputIDsTensor = TensorUtils.convertIntArrayToTensor(onnxEnv, input_ids);
            OnnxTensor encoderAttentionMaskTensor = TensorUtils.convertIntArrayToTensor(onnxEnv, input.getAttentionMask());
            OnnxTensor decoderOutput = null;
            Map<String,OnnxTensor> decoderInput = new HashMap<String,OnnxTensor>();
            float[][][] value = null;
            float [] outputValues = null;
            int[] outputIDs = null;
            //we prepare the input of the cache initializer
            Map<String,OnnxTensor> initInput = new HashMap<String,OnnxTensor>();
            initInput.put("encoder_hidden_states", encoderResult);
            //cache initializer execution
            OrtSession.Result initResult = null;
            initResult = cacheInitSession.run(initInput);
            android.util.Log.i("performance", "Cache initialization done in: " + (System.currentTimeMillis()-time) + "ms");

            //we begin the iterative execution of the decoder
            OrtSession.Result result = null;
            OrtSession.Result oldResult = null;
            OnnxTensor emptyPreLogits = TensorUtils.createFloatTensorWithSingleValue(onnxEnv, 0, new long[]{EMPTY_BATCH_SIZE, 1, 1024});
            OnnxTensor emptyInputIds = TensorUtils.createInt64TensorWithSingleValue(onnxEnv, 0, new long[]{EMPTY_BATCH_SIZE, 2});
            int max = -1;
            int j = 1;
            while(max != eos){
                initialTime = System.currentTimeMillis();
                time = System.currentTimeMillis();
                //we prepare the decoder input
                decoderInput = new HashMap<String,OnnxTensor>();
                decoderInput.put("input_ids", inputIDsTensor);
                decoderInput.put("encoder_attention_mask", encoderAttentionMaskTensor);
                OrtSession.Result embedResult = null;

                {
                    //we do the embedding separately and then we pass the result to the encoder
                    Map<String, OnnxTensor> embedInput = new HashMap<String, OnnxTensor>();
                    embedInput.put("input_ids", inputIDsTensor);
                    embedInput.put("pre_logits", emptyPreLogits);
                    embedInput.put("use_lm_head", TensorUtils.convertBooleanToTensor(onnxEnv, false));
                    ArraySet<String> requestedOutputs = new ArraySet<>();
                    requestedOutputs.add("embed_matrix");
                    embedResult = embedAndLmHeadSession.run(embedInput, requestedOutputs);

                    decoderInput.put("embed_matrix", (OnnxTensor) embedResult.get(0));
                }

                if(j == 1){
                    long[] shape = {1, 16, 0, hiddenSize};
                    OnnxTensor decoderPastTensor = TensorUtils.createFloatTensorWithSingleValue(onnxEnv, 0, shape);
                    for (int i = 0; i < nLayers; i++) {
                        decoderInput.put("past_key_values." + i + ".decoder.key", decoderPastTensor);
                        decoderInput.put("past_key_values." + i + ".decoder.value", decoderPastTensor);
                        decoderInput.put("past_key_values." + i + ".encoder.key", (OnnxTensor) initResult.get("present." + i + ".encoder.key").get());
                        decoderInput.put("past_key_values." + i + ".encoder.value", (OnnxTensor) initResult.get("present." + i + ".encoder.value").get());
                    }
                }else {
                    for (int i = 0; i < nLayers; i++) {
                        decoderInput.put("past_key_values." + i + ".decoder.key", (OnnxTensor) result.get("present." + i + ".decoder.key").get());
                        decoderInput.put("past_key_values." + i + ".decoder.value", (OnnxTensor) result.get("present." + i + ".decoder.value").get());
                        decoderInput.put("past_key_values." + i + ".encoder.key", (OnnxTensor) initResult.get("present." + i + ".encoder.key").get());
                        decoderInput.put("past_key_values." + i + ".encoder.value", (OnnxTensor) initResult.get("present." + i + ".encoder.value").get());
                    }
                }
                oldResult = result;
                android.util.Log.i("performance", "pre-execution of"+j+"th word done in: " + (System.currentTimeMillis()-time) + "ms");
                time = System.currentTimeMillis();
                //decoder execution (with cache)
                result = decoderSession.run(decoderInput);

                android.util.Log.i("performance", "execution of"+j+"th word done in: " + (System.currentTimeMillis()-time) + "ms");
                time = System.currentTimeMillis();

                if(oldResult != null) {
                    oldResult.close(); //serves to release the memory occupied by the result (otherwise it accumulates and increases a lot)
                    android.util.Log.i("performance", "release RAM of"+j+"th word done in: " + (System.currentTimeMillis()-time) + "ms");
                }

                embedResult.close();

                //we take the logits and the max value
                OrtSession.Result lmHeadResult = null;

                {
                    //we execute the lmHead separately to get the logits
                    Map<String, OnnxTensor> lmHeadInput = new HashMap<String, OnnxTensor>();
                    lmHeadInput.put("input_ids", emptyInputIds);
                    lmHeadInput.put("pre_logits", (OnnxTensor) result.get("pre_logits").get());
                    lmHeadInput.put("use_lm_head", TensorUtils.convertBooleanToTensor(onnxEnv, true));
                    ArraySet<String> requestedOutputs = new ArraySet<>();
                    requestedOutputs.add("logits");
                    lmHeadResult = embedAndLmHeadSession.run(lmHeadInput, requestedOutputs);
                    decoderOutput = (OnnxTensor) lmHeadResult.get(0);
                }

                value = (float[][][]) decoderOutput.getValue();
                outputValues = value[0][0];
                max = NNUtils.getIndexOfLargest(outputValues);
                completeOutput.add(max);

                lmHeadResult.close();

                //we prepare the inputs of the next iteration
                if(j == 1) {
                    input_ids[0] = tokenizer.getLanguageID(getNllbLanguageCode(outputLanguage.getCode()));
                } else {
                    input_ids[0] = max;
                }

                inputIDsTensor = TensorUtils.convertIntArrayToTensor(onnxEnv, input_ids);
                android.util.Log.i("performance", "post-execution of"+j+"th word done in: " + (System.currentTimeMillis()-time) + "ms");
                android.util.Log.i("performance", "Generation of"+j+"th word done in: " + (System.currentTimeMillis() - initialTime) + "ms");
                //we return the partial result
                outputIDs = completeOutput.stream().mapToInt(i -> i).toArray();
                String partialResult = tokenizer.decode(outputIDs);

                responseListener.onTranslatedText(partialResult, currentResultID, false, outputLanguage);

                android.util.Log.i("result", "部分"+partialResult);
                j++;
            }
            if(result != null) {
                result.close();
            }
            initResult.close();

        } catch (OrtException e) {
            e.printStackTrace();
            Log.e("error", "ERROR_EXECUTING_MODEL");
            responseListener.onFailure(e);
        }
    }


    private String performTextTranslation(final String textToTranslate, final CustomLocale inputLanguage, final CustomLocale outputLanguage) {
        // 获取开始时间
        long initTime = System.currentTimeMillis();

        // 打印日志，打印待翻译文本
        android.util.Log.i("result", "Translation input: " + textToTranslate);

        // 使用BreakIterator将输入文本分割成句子
        ArrayList<String> textSplit = new ArrayList<>();
        // 设置待翻译文本的语言
        BreakIterator iterator = BreakIterator.getSentenceInstance(inputLanguage.getLocale());
        iterator.setText(textToTranslate);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            textSplit.add(textToTranslate.substring(start,end));
        }
        // 合并那些加起来不超过最大输入大小的相邻句子
        boolean joined = true;
        while (joined) {
            joined = false;
            for (int i = 1; i < textSplit.size(); i++) {
                int numTokens = tokenizer.tokenize(getNllbLanguageCode(inputLanguage.getCode()), getNllbLanguageCode(outputLanguage.getCode()), textSplit.get(i-1)).getInputIDs().length;
                int numTokens2 = tokenizer.tokenize(getNllbLanguageCode(inputLanguage.getCode()), getNllbLanguageCode(outputLanguage.getCode()), textSplit.get(i)).getInputIDs().length;
                if ((numTokens + numTokens2 < 200) || (numTokens2 < 5)) {
                    textSplit.set(i-1, textSplit.get(i-1) + textSplit.get(i));
                    textSplit.remove(i);
                    i = i - 1;
                    joined = true;
                }
            }
        }

        // 打印分词日志
        android.util.Log.i("result", "Input text splitted in "+textSplit.size()+" subtexts:");
        for (String subtext : textSplit) {
            android.util.Log.i("result", subtext);
        }
        android.util.Log.i("performance", "Text split done in: " + (System.currentTimeMillis() - initTime) + "ms");

        // 用于存储最终连接后的翻译结果
        final String[] joinedStringOutput = {""};

        // 分段来进行翻译
        for(int i=0; i<textSplit.size(); i++) {
            // 用于存储beam search的输出结果
            ArrayList<Integer>[] completeBeamOutput = new ArrayList[BEAM_SIZE];  //contains the "beamSize" strings produced by the decoder
            // 初始化beam search的输出数组
            for (int j = 0; j < BEAM_SIZE; j++) {
                completeBeamOutput[j] = new ArrayList<Integer>();
            }
            // 存储每个beam的概率
            double[] beamsOutputsProbabilities = new double[BEAM_SIZE];  //contains for each of the "beamSize" strings produced by the decoder its overall probability

            // 分词处理
            long time = System.currentTimeMillis();
            TokenizerResult input = null;

            // NLLB模型的分词处理
            android.util.Log.i("result","mode == NLLB_CACHE");
            input = tokenizer.tokenize(
                    getNllbLanguageCode(inputLanguage.getCode()),
                    getNllbLanguageCode(outputLanguage.getCode()),
                    textSplit.get(i)
            );


            // 打印分词所用时间
            android.util.Log.i("performance", "Tokenization done in: " + (System.currentTimeMillis() - time) + "ms");


            // 编码器(Encoder)执行
            time = System.currentTimeMillis();
            OnnxTensor encoderResult = executeEncoder(input.getInputIDs(), input.getAttentionMask());
            android.util.Log.i("performance", "Encoder done in: " + (System.currentTimeMillis() - time) + "ms");

            // 检查编码器执行是否成功，失败则进行回调
            if(encoderResult == null){
                android.util.Log.e("编码器执行失败", "encoderResult == null");
                return "encoderResult failed";
            }

            // 解码器初始化
            final int eos = tokenizer.PieceToID("</s>"); // 获取结束符的token ID
            ArrayList<Integer> completeOutput = new ArrayList<Integer>();
            // 添加开始符<s>的token ID

            completeOutput.add(0);   //tokenizer.PieceToID("<s>")

            // Greedy Search实现
            executeCacheDecoderGreedy(input, encoderResult, completeOutput, outputLanguage, new TranslateListener() {
                @Override
                public void onTranslatedText(String text, long resultID, boolean isFinal, CustomLocale languageOfText) {

                    // 处理翻译文本
                    String outputText;
                    if(joinedStringOutput[0].equals("")){
                        outputText = joinedStringOutput[0] + text;
                    }else {
                        outputText = joinedStringOutput[0] + " " + text;
                    }


                    // 通知结果
                    final long currentResultIDCopy = currentResultID;  //we do a copy because otherwise the currentResultID is incremented before notifying the message (due to the notification being executed in the mainThread)
//                    if (responseListener != null) {
//                        mainHandler.post(() -> responseListener.onTranslatedText(outputText, currentResultIDCopy, false, outputLanguage));
//                    } else {
//                        mainHandler.post(() -> notifyResult(outputText, currentResultIDCopy, false, outputLanguage));
//                    }
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Log.e("preformTextTranslation", "翻译失败");
                }
            });


            //we convert the ids of completeOutputs into a string and return it
            // 释放编码器资源
            encoderResult.close();
            int[] completeOutputArray;


            android.util.Log.i("normal", "最普通的结果");
            completeOutputArray = completeOutput.stream().mapToInt(k -> k).toArray();  //converte completeOutput in un array di int


            // 将token ID转换回文本
            String finalSplitResult = tokenizer.decode(completeOutputArray);

            // 拼接部分翻译结果
            if(joinedStringOutput[0].equals("")){
                joinedStringOutput[0] = joinedStringOutput[0] + finalSplitResult;
            }else {
                joinedStringOutput[0] = joinedStringOutput[0] + " " + finalSplitResult;
            }

        }
        long time = System.currentTimeMillis();
        //String finalResult = tokenizer.decode(completeOutputArray);
        String finalResult = joinedStringOutput[0];

        // 记录性能日志
        android.util.Log.i("performance", "Detokenization done in: " + (System.currentTimeMillis() - time) + "ms");
        android.util.Log.i("performance", "TRANSLATION DONE IN: " + (System.currentTimeMillis() - initTime) + "ms");

        // 通知最终结果
        final long currentResultIDCopy = currentResultID;  //we do a copy because otherwise the currentResultID is incremented before notifying the message (due to the notification being executed in the mainThread)

        currentResultID++;
        return finalResult;
    }


}

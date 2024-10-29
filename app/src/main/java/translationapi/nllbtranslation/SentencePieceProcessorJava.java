package translationapi.nllbtranslation;

public class SentencePieceProcessorJava {
    //specialTokens is an array containing the symbols that sentencepiece does not translate correctly (their index is equal to their ID)
    private final String[] specialTokens = {"<s>", "<pad>", "</s>", "<unk>"};

    //Used to load SentencePieceProcessorInterface.cpp on application startup
    static {
        System.loadLibrary("sentencepiece");
    }

    private final long spProcessorPointer;

    public SentencePieceProcessorJava(){
        spProcessorPointer=SentencePieceProcessorNative();
    }

    public void Load(String vocab_file){
        LoadNative(spProcessorPointer,vocab_file);
    }
    public int[] encode(String text){
        return encodeNative(spProcessorPointer,text);
    }
    public int PieceToID(String token){
        for (int i=0; i < specialTokens.length; i++){
            if(token.equals(specialTokens[i])){
                return i;
            }
        }
        return PieceToIDNative(spProcessorPointer,token)+1;
    }

    public String IDToPiece(int id){
        return IDToPieceNative(spProcessorPointer,id);
    }

    //this method not work (we convert one token at a time using IdToPiece to do the decode)
    /*public String decode(int[] ids){
        String text = decodeNative(spProcessorPointer,ids);
        if(text.charAt(0) == '_'){
            text = text.substring(1);
        }
        return text.replace('_', ' ');
    }*/

    private native long SentencePieceProcessorNative();
    private native void LoadNative(long processor, String vocab_file);
    private native int[] encodeNative(long processor, String text);
    private native int PieceToIDNative(long processor, String token);
    public native String IDToPieceNative(long processor, int id);
    private native String decodeNative(long processor, int[] ids);
}

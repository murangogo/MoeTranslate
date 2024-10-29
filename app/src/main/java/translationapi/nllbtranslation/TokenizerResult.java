package translationapi.nllbtranslation;

public class TokenizerResult {
    private int[] inputIDs;
    private int[] attentionMask;

    public TokenizerResult(int[] inputIDs, int[] attentionMask) {
        this.inputIDs = inputIDs;
        this.attentionMask = attentionMask;
    }


    public int[] getInputIDs() {
        return inputIDs;
    }

    public void setInputIDs(int[] inputIDs) {
        this.inputIDs = inputIDs;
    }

    public int[] getAttentionMask() {
        return attentionMask;
    }

    public void setAttentionMask(int[] attentionMask) {
        this.attentionMask = attentionMask;
    }
}

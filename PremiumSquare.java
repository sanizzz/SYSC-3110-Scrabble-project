public enum PremiumSquare {
    NORMAL(1, 1),
    DOUBLE_LETTER(2, 1),
    TRIPLE_LETTER(3, 1),
    DOUBLE_WORD(1, 2),
    TRIPLE_WORD(1, 3);

    private final int letterMultiplier;
    private final int wordMultiplier;

    PremiumSquare(int letterMultiplier, int wordMultiplier) {
        this.letterMultiplier = letterMultiplier;
        this.wordMultiplier = wordMultiplier;
    }

    public int letterMultiplier() {
        return letterMultiplier;
    }

    public int wordMultiplier() {
        return wordMultiplier;
    }

    public static PremiumSquare fromToken(String token) {
        if (token == null || token.isEmpty()) {
            return NORMAL;
        }
        switch (token.toUpperCase()) {
            case "DL":
            case "DOUBLE_LETTER":
                return DOUBLE_LETTER;
            case "TL":
            case "TRIPLE_LETTER":
                return TRIPLE_LETTER;
            case "DW":
            case "DOUBLE_WORD":
                return DOUBLE_WORD;
            case "TW":
            case "TRIPLE_WORD":
                return TRIPLE_WORD;
            default:
                return NORMAL;
        }
    }
}


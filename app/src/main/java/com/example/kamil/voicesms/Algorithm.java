package com.example.kamil.voicesms;

public class Algorithm {

    public static String processingText(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            findPunctuationMarks(parts, i);
            findEmoticons(parts, i);
            deleting(parts, i);
        }
        moveEmptyPartsToEnd(parts);
        return createTextFromParts(parts);
    }

    private static void findEmoticons(String[] parts, int i) {
        if (parts[i].equalsIgnoreCase("emotikon")) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("uśmiech")) {
                    parts[i] = ":)";
                    parts[i + 1] = "";
                } else if (parts[i + 1].equalsIgnoreCase("smutny")) {
                    parts[i] = ":(";
                    parts[i + 1] = "";
                } else if (parts[i + 1].equalsIgnoreCase("język")) {
                    parts[i] = ":P";
                    parts[i + 1] = "";
                }
            }
        }
    }

    private static void findPunctuationMarks(String[] parts, int i) {
        if (parts[i].equalsIgnoreCase("kropka")) {
            parts[i] = ".";
        } else if (parts[i].equalsIgnoreCase("przecinek")) {
            parts[i] = ",";
        } else if (parts[i].equalsIgnoreCase("znak")) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("zapytania")) {
                    parts[i] = "?";
                    parts[i + 1] = "";
                }
            }
        } else if (parts[i].equalsIgnoreCase("wykrzyknik")) {
            parts[i] = "!";
        } else if (parts[i].equalsIgnoreCase("cudzysłów")) {
            parts[i] = "\"";
        } else if (parts[i].equalsIgnoreCase("dwukropek")) {
            parts[i] = ":";
        } else if (parts[i].equalsIgnoreCase("myślnik")) {
            parts[i] = "-";
        } else if (parts[i].equalsIgnoreCase("otwórz")) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("nawias")) {
                    parts[i] = "(";
                    parts[i + 1] = "";
                }
            }
        }  else if (parts[i].equalsIgnoreCase("zamknij")) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("nawias")) {
                    parts[i] = ")";
                    parts[i + 1] = "";
                }
            }
        }
    }

    private static void deleting(String[] parts, int i) {
        if (parts[i].equalsIgnoreCase(("usuń"))) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("wyraz")) {
                    deleteWord(parts, i);
                } else if (parts[i + 1].equalsIgnoreCase("zdanie")) {
                    deleteSentence(parts, i);
                } else if (parts[i + 1].equalsIgnoreCase("wszystko")) {
                    deleteAllMessage(parts, i);
                }
            }
        }
    }

    private static void deleteWord(String[] parts, int i) {
        parts[i] = "";
        parts[i + 1] = "";
        if (i == 0 && i == parts.length - 2) {
            deleteLastWordFromReadyMessageText();
        } else {
            if (!parts[i - 1].equals("")) {
                parts[i - 1] = "";
            } else {
                for (int j = i - 2; j >= 0; j--) {
                    if (!parts[j].equals("")) {
                        parts[j] = "";
                        break;
                    }
                    if (j == 0 && i == parts.length - 2) {
                        deleteLastWordFromReadyMessageText();
                    }
                }
            }
        }
    }

    private static void deleteLastWordFromReadyMessageText() {
        String[] message = MainActivity.readyMessageText.split(" ");
        MainActivity.readyMessageText = "";
        for (int j = 0; j < message.length - 1; j++) {
            MainActivity.readyMessageText += message[j];
            if (j != message.length - 2) {
                MainActivity.readyMessageText += " ";
            }
        }
    }

    private static void deleteSentence(String[] parts, int i) {
        parts[i] = "";
        parts[i + 1] = "";
        if (i == 0 && i == parts.length - 2) {
            deleteLastSentenceFromReadyMessageText();
        } else {
            int index = -1;
            int indexLastWord = findIndexOfLastWord(parts, i);

            /** Nie efektywne!! Trzeba szukać od końca i break!  */
            for (int j = 0; j < indexLastWord; j++) {
                if (isEqualEndSentence(parts[j])) {
                    index = j;
                }
            }
            if (index > -1) {
                for (int j = index + 1; j < i; j++) {
                    parts[j] = "";
                }
            } else if (index == -1 && indexLastWord != -1) {
                for (int j = 0; j < i; j++) {
                    parts[j] = "";
                }
                index = findIndexOfEndSentence();
                if (index != MainActivity.readyMessageText.length() - 1) {
                    deleteLastSentenceFromReadyMessageText();
                }
            } else if (i == parts.length - 2) {
                deleteLastSentenceFromReadyMessageText();
            }
        }
    }

    private static int findIndexOfLastWord(String[] parts, int i) {
        int index = -1;
        for (int k = i - 1; k >= 0; k--) {
            if (!parts[k].equals("")) {
                index = k;
                break;
            }
        }
        return index;
    }

    private static void deleteLastSentenceFromReadyMessageText() {
        int index = findIndexOfEndSentence();
        if (index == -1) {
            MainActivity.readyMessageText = "";
        } else if (index == MainActivity.readyMessageText.length() - 1) {
            MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index);
            index = findIndexOfEndSentence();
            if (index > -1) {
                MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index + 1);
            } else {
                MainActivity.readyMessageText = "";
            }
        } else {
            MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index + 1);
        }
    }

    private static void deleteAllMessage(String[] parts, int i) {
        MainActivity.readyMessageText = "";
        for (int j = 0; j < i + 2; j++) {
            parts[j] = "";
        }
    }

    private static void moveEmptyPartsToEnd(String[] parts) {
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("")) {
                for (int j = i + 1; j < parts.length; j++) {
                    if (!parts[j].equals("")) {
                        parts[i] = parts[j];
                        parts[j] = "";
                        break;
                    }
                }
            }
        }
    }

    private static String createTextFromParts(String[] parts) {
        String text = "";
        for (int i=0; i<parts.length; i++) {
            if (parts[0].equals("")) {
                return text;
            }
            if (i == 0 && MainActivity.readyMessageText.equals("")) {   // Pierwszy wyraz wiadomości
                text += myToUpperCase(parts[i]);
            } else if (i == 0 && !isEqualEndSentence(parts[i])) {
                char index = MainActivity.readyMessageText.charAt(MainActivity.readyMessageText.length() - 1);
                text += " ";
                if (isEqualEndSentence(String.valueOf(index))) {
                    text += myToUpperCase(parts[i]);
                } else {
                    text += parts[i];
                }
            } else if (isEqualEndSentence(parts[i])) {
                if (i + 1 < parts.length && !parts[i + 1].equals("")) {
                    parts[i + 1] = myToUpperCase(parts[i + 1]);
                }
                text += parts[i];
            } else if (parts[i].equals(",") || parts[i].equals(":") || parts[i].equals(")")) {
                text += parts[i];
            } else if (parts[i-1].equals("(")) {
                text += parts[i];
            } else if (!parts[i].equals("")) {
                text += " " + parts[i];
            }
        }
        return text;
    }

    private static boolean isEqualEndSentence(String part) {
        return part.equals(".") || part.equals("?") || part.equals("!");
    }

    private static String myToUpperCase(String str) {
        return Character.toUpperCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1) : "");
    }

    private static int findIndexOfEndSentence() {
        int index = MainActivity.readyMessageText.lastIndexOf(".");
        if (index < MainActivity.readyMessageText.lastIndexOf("?")) {
            index = MainActivity.readyMessageText.lastIndexOf("?");
        }
        if (index < MainActivity.readyMessageText.lastIndexOf("!")) {
            index = MainActivity.readyMessageText.lastIndexOf("!");
        }
        return index;
    }

}
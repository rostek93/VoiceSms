package com.example.kamil.voicesms;

/**
 * Created by Kamil Rostecki
 */
public class Algorithm {

    static String processingText(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            findPunctuationMarks(parts, i);
            findEmoticons(parts, i);
            deleting(parts, i);
        }
        moveEmptyPartsAtEnd(parts);
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
            if (i + 1 < parts.length)
                parts[i + 1] = Character.toUpperCase(parts[i + 1].charAt(0)) + (parts[i + 1].length() > 1 ? parts[i + 1].substring(1) : "");
        }

        else if (parts[i].equalsIgnoreCase("przecinek"))
            parts[i] = ",";

        else if (parts[i].equalsIgnoreCase("znak")) {
            if (i + 1 < parts.length)
                if (parts[i + 1].equalsIgnoreCase("zapytania")) {
                    parts[i] = "?";
                    parts[i + 1] = "";
                    if (i + 2 < parts.length)
                        parts[i + 2] = Character.toUpperCase(parts[i + 2].charAt(0)) + (parts[i + 2].length() > 1 ? parts[i + 2].substring(1) : "");
                }
        }

        else if (parts[i].equalsIgnoreCase("wykrzyknik")) {
            parts[i] = "!";
            if (i + 1 < parts.length)
                parts[i + 1] = Character.toUpperCase(parts[i + 1].charAt(0)) + (parts[i + 1].length() > 1 ? parts[i + 1].substring(1) : "");
        }

        else if (parts[i].equalsIgnoreCase("cudzysłów"))
            parts[i] = "\"";

        else if (parts[i].equalsIgnoreCase("dwukropek"))
            parts[i] = ":";

        else if (parts[i].equalsIgnoreCase("myślnik"))
            parts[i] = "-";

        else if (parts[i].equalsIgnoreCase("otwórz")) {
            if (i + 1 < parts.length)
                if (parts[i + 1].equalsIgnoreCase("nawias")) {
                    parts[i] = "(";
                    parts[i + 1] = "";
                }
        }

        else if (parts[i].equalsIgnoreCase("zamknij")) {
            if (i + 1 < parts.length)
                if (parts[i + 1].equalsIgnoreCase("nawias")) {
                    parts[i] = ")";
                    parts[i + 1] = "";
                }
        }
    }

    private static void deleting(String[] parts, int i) {
        if (parts[i].equalsIgnoreCase(("usuń"))) {
            if (i + 1 < parts.length) {
                if (parts[i + 1].equalsIgnoreCase("wyraz")) {
                    if (i == 0) {
                        String[] message = MainActivity.readyMessageText.split(" ");
                        MainActivity.readyMessageText = "";
                        for (int j = 0; j < message.length - 1; j++) {
                            MainActivity.readyMessageText += message[j];
                            if (j != message.length - 2)
                                MainActivity.readyMessageText += " ";
                        }
                        parts[i] = "";
                        parts[i + 1] = "";
                    } else {
                        if (parts[i - 1].equals("")) {
                            for (int j = i - 2; j >= 0; j--) {
                                if (!parts[j].equals("")) {
                                    parts[j] = "";
                                    break;
                                }
                            }
                        } else
                            parts[i - 1] = "";
                        parts[i] = "";
                        parts[i + 1] = "";
                    }
                }

                else if (parts[i + 1].equalsIgnoreCase("zdanie")) {
                    if (i == 0) {
                        int index = MainActivity.readyMessageText.lastIndexOf(".");
                        if (index < MainActivity.readyMessageText.lastIndexOf("?"))
                            index = MainActivity.readyMessageText.lastIndexOf("?");
                        if (index < MainActivity.readyMessageText.lastIndexOf("!"))
                            index = MainActivity.readyMessageText.lastIndexOf("!");

                        if (index > -1 && MainActivity.readyMessageText.length() - 1 != index)
                            MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index + 1);
                        else if (index == MainActivity.readyMessageText.length() - 1) {
                            MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index);
                            index = MainActivity.readyMessageText.lastIndexOf(".");
                            if (index < MainActivity.readyMessageText.lastIndexOf("?"))
                                index = MainActivity.readyMessageText.lastIndexOf("?");
                            if (index < MainActivity.readyMessageText.lastIndexOf("!"))
                                index = MainActivity.readyMessageText.lastIndexOf("!");
                            if (index > -1)
                                MainActivity.readyMessageText = MainActivity.readyMessageText.substring(0, index + 1);
                            else {
                                for (int j = 0; j > parts.length; j++)
                                    parts[j] = "";
                            }
                        }
                        parts[i] = "";
                        parts[i + 1] = "";
                    } else {
                        int index = -1;
                        int indexLastWord = i;

                        for (int k = i - 1; k >= 0; k--) {
                            if (!parts[k].equals("")) {
                                indexLastWord = k;
                                break;
                            }
                        }

                        for (int j = 0; j < indexLastWord; j++)
                            if (parts[j].equals(".") || parts[j].equals("?") || parts[j].equals("!"))
                                index = j;

                        if (index > -1) {
                            for (int j = index + 1; j < i + 2; j++)
                                parts[j] = "";
                            if (i + 2 < parts.length)
                                parts[i + 2] = Character.toUpperCase(parts[i + 2].charAt(0)) + (parts[i + 2].length() > 1 ? parts[i + 2].substring(1) : "");
                        } else {
                            for (int j = 0; j < i + 2; j++)
                                parts[j] = "";
                        }
                    }

                } else if (parts[i + 1].equalsIgnoreCase("wszystko")) {
                    MainActivity.readyMessageText = "";
                    for (int j = 0; j < i + 2; j++)
                        parts[j] = "";
                }
            }
        }
    }

    private static void moveEmptyPartsAtEnd(String[] parts) {
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
            if (parts[0].equals(""))
                return text;
            if (i == 0 && MainActivity.readyMessageText.equals("")) // Pierwszy wyraz wiadomości
                text += Character.toUpperCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
            else if (i == 0 && !parts[i].equals("?") && !parts[i].equals(".") && !parts[i].equals("!") && !parts[i].equals(",")) {
                char index = MainActivity.readyMessageText.charAt(MainActivity.readyMessageText.length()-1);
                text += " ";
                if (index == '.' || index == '?' || index == '!')
                    text += Character.toUpperCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
                else
                    text += Character.toLowerCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
            } else if (parts[i].equals(".") || parts[i].equals("?") || parts[i].equals("!") || parts[i].equals(",") || parts[i].equals(":")) {
                text += parts[i];
            } else if (parts[i-1].equals("(")) {
                text += parts[i];
            } else if (parts[i].equals(")")) {
                text += parts[i];
            } else if (!parts[i].equals("")) {
                text += " " + parts[i];
            }
        }
        return text;
    }

}
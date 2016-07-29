package com.example.kamil.voicesms;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.jar.Manifest;

/**
 * Created by Kamil on 2016-06-04.
 */
public class Algorithm {

    static String interpunction(String text) {
        String[] parts = text.split(" ");
        text = "";

        for (int i=0; i < parts.length; i++) {
            parts[i] = parts[i].toLowerCase();
          //  parts[i] = Character.toLowerCase(parts[i].charAt(0)) + (parts[i].length() > 1 ? parts[i].substring(1) : "");
        }

        for (int i=0; i<parts.length; i++) {

            if(parts[i].equalsIgnoreCase("kropka")) {
                parts[i] = ".";
                if (i+1 < parts.length)
                    if (!parts[i+1].equals("przecinek") && !parts[i+1].equals("znak") && !parts[i+1].equals("wykrzyknik"))
                        parts[i+1] = Character.toUpperCase(parts[i+1].charAt(0)) + (parts[i+1].length() > 1 ? parts[i+1].substring(1) : "");
            }

            else if (parts[i].equalsIgnoreCase("przecinek")) {
                parts[i] = ",";
            }

            else if (parts[i].equalsIgnoreCase("znak")) {
                if (i+1 < parts.length)
                    if (parts[i+1].equals("zapytania")) {
                        parts[i] = "?";
                        parts[i+1] = "";
                        if (i+2 < parts.length)
                            if (!parts[i+2].equals("kropka") && !parts[i+2].equals("przecinek") && !parts[i+2].equals("wykrzyknik"))
                                parts[i+2] = Character.toUpperCase(parts[i+2].charAt(0)) + (parts[i+2].length() > 1 ? parts[i+2].substring(1) : "");
                    }
            }

            else if (parts[i].equalsIgnoreCase("wykrzyknik")) {
                parts[i] = "!";
                if (i+1 < parts.length)
                    if (!parts[i+1].equals("kropka") && !parts[i+1].equals("znak") && !parts[i+1].equals("przecinek"))
                        parts[i+1] = Character.toUpperCase(parts[i+1].charAt(0)) + (parts[i+1].length() > 1 ? parts[i+1].substring(1) : "");
            }

            else if (parts[i].equalsIgnoreCase(("usuń"))) {
                if (i+1 < parts.length) {
                    if (parts[i + 1].equals("wyraz")) {
                        if (i == 0) {
                            String[] message = MainActivity.messageText.split(" ");
                            MainActivity.messageText = "";
                            for (int j = 0; j < message.length - 1; j++) {
                                MainActivity.messageText += message[j];
                                if (j != message.length-2)
                                    MainActivity.messageText += " ";
                            }
                            parts[i] = "";
                            parts[i + 1] = "";
                        } else {
                            if (parts[i-1].equals("")) {
                                for (int j = i - 2; j >= 0; j--) {
                                    if (!parts[j].equals("")) {
                                        parts[j] = "";
                                        break;
                                    }
                                }
                            }
                            else
                                parts[i - 1] = "";
                            parts[i] = "";
                            parts[i + 1] = "";
                        }

                    } else if (parts[i + 1].equals("zdanie")) {
                        if (i == 0) {
                            int index = MainActivity.messageText.lastIndexOf(".");
                            if (index < MainActivity.messageText.lastIndexOf("?"))
                                index = MainActivity.messageText.lastIndexOf("?");
                            if (index < MainActivity.messageText.lastIndexOf("!"))
                                index = MainActivity.messageText.lastIndexOf("!");

                            if (index > -1 && MainActivity.messageText.length()-1 != index)
                                MainActivity.messageText = MainActivity.messageText.substring(0, index+1);
                            else if (index == MainActivity.messageText.length()-1) {
                                MainActivity.messageText = MainActivity.messageText.substring(0, index);
                                index = MainActivity.messageText.lastIndexOf(".");
                                if (index < MainActivity.messageText.lastIndexOf("?"))
                                    index = MainActivity.messageText.lastIndexOf("?");
                                if (index < MainActivity.messageText.lastIndexOf("!"))
                                    index = MainActivity.messageText.lastIndexOf("!");
                                if (index > -1)
                                    MainActivity.messageText = MainActivity.messageText.substring(0, index + 1);
                                else {
                                    for (int j = 0; j > parts.length; j++)
                                        parts[j] = "";
                                }
                            }
                            parts[i] = "";
                            parts[i + 1] = "";
                        } else {
                            String temp = "";
                            for (int j = 0; j < i+1; j++)
                                temp += (parts[j] + " ");
                            int index = temp.lastIndexOf(".");
                            if (index < temp.lastIndexOf("?"))
                                index = temp.lastIndexOf("?");
                            if (index < temp.lastIndexOf("!"))
                                index = temp.lastIndexOf("!");
                            if (index > -1) {
                                temp = temp.substring(0, index);
                                int count = temp.length() - temp.replace(" ", "").length();
                                for (int j = count+1; j < i+2; j++)
                                    parts[j] = "";
                                if (i+2 < parts.length)
                                    parts[i+2] = Character.toUpperCase(parts[i+2].charAt(0)) + (parts[i+2].length() > 1 ? parts[i+2].substring(1) : "");
                            } else {
                                for (int j = 0; j < i+2; j++)
                                    parts[j] = "";
                            }
                        }

                    } else if (parts[i + 1].equals("wszystko")) {
                        if (i == 0) {
                            MainActivity.messageText = "";
                            parts[i] = "";
                            parts[i + 1] = "";
                        } else {
                            for (int j = 0; j < i+2; j++) {
                                parts[j] = "";
                            }
                        }
                    }
                }
            }
        }

        for (int i=0; i<parts.length-1; i++) {
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

        for (int i=0; i<parts.length; i++) {
            if (parts[0].equals(""))
                return MainActivity.messageText;
            if (i == 0 && MainActivity.messageText.equals(""))
                text += Character.toUpperCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
            else if (i == 0 && !parts[i].equals("?") && !parts[i].equals(".") && !parts[i].equals("!") && !parts[i].equals(",")) {
                char index = MainActivity.messageText.charAt(MainActivity.messageText.length()-1);
                text += " ";
                if (index == '.' || index == '?' || index == '!')
                    text += Character.toUpperCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
                else
                    text += Character.toLowerCase(parts[0].charAt(0)) + (parts[0].length() > 1 ? parts[0].substring(1) : "");
            } else if (parts[i].equals(".") || parts[i].equals("?") || parts[i].equals("!") || parts[i].equals(",")) {
                text += parts[i];
            } else if (!parts[i].equals(""))
                text += " " + parts[i];
        }
        return MainActivity.messageText + text;
    }

}

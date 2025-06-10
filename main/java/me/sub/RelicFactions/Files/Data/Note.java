package me.sub.RelicFactions.Files.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Note {

    private int id;
    private final UUID noteGiver;
    private final long timeStamp;
    private final String text;

    public Note(int id, UUID noteGiver, String text) {
        this(id, noteGiver, text, System.currentTimeMillis());
    }

    public Note(int id, UUID noteGiver, String text, long timeStamp) {
        this.id = id;
        this.noteGiver = noteGiver;
        this.text = text;
        this.timeStamp = timeStamp;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public UUID getNoteGiver() {
        return noteGiver;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", noteGiver=" + noteGiver +
                ", timeStamp=" + timeStamp +
                ", text='" + text + '\'' +
                '}';
    }

    public String serialize() {
        String safeText = text.replace("\\", "\\\\").replace("|", "\\|");
        return id + "|" + noteGiver.toString() + "|" + timeStamp + "|" + safeText;
    }


    public static Note deserialize(String str) {
        String[] parts = str.split("(?<!\\\\)\\|", 4);
        if (parts.length != 4) throw new IllegalArgumentException("Invalid note string");

        int id = Integer.parseInt(parts[0]);
        UUID noteGiver = UUID.fromString(parts[1]);
        long timeStamp = Long.parseLong(parts[2]);
        String text = parts[3].replace("\\|", "|").replace("\\\\", "\\");

        return new Note(id, noteGiver, text, timeStamp);
    }
    public static String serializeList(List<Note> notes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
            String noteStr = notes.get(i).serialize().replace(";", "\\;");
            sb.append(noteStr);
            if (i < notes.size() - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public static ArrayList<Note> deserializeList(String s) {
        ArrayList<Note> notes = new ArrayList<>();
        if (s == null || s.isEmpty()) return notes;

        String[] parts = s.split("(?<!\\\\);");
        for (String part : parts) {
            String noteStr = part.replace("\\;", ";");
            notes.add(Note.deserialize(noteStr));
        }
        return notes;
    }
}
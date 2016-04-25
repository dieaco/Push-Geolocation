package com.entuizer.push.models;

import android.graphics.Bitmap;

/**
 * Created by Diego Acosta on 31/03/2016.
 */
public class Message {

    // Atributos
    private String id;
    private String mensaje;
    private String timestamp;
    private int isRead;
    private int userId;
    private Bitmap picture;

    public Message() {
    }

    public Message(String id, String mensaje, String timestamp, int isRead, int userId, Bitmap picture) {
        this.id = id;
        this.mensaje = mensaje;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.userId = userId;
        this.picture = picture;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMensaje() {
        return this.mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getIsRead() {
        return this.isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Bitmap getPicture() {
        return this.picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

}

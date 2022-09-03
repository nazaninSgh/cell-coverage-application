package com.example.nazanin.finalproject.model.DTO;

import android.arch.persistence.room.*;

@Entity(primaryKeys= {"lon", "lat" })
public class Lte {

    @ColumnInfo(name = "lon")
    private double lon;

    @ColumnInfo(name = "lat")
    private double lat;

    @ColumnInfo(name = "tac")
    private int tac;

    @ColumnInfo(name = "rsrp")
    private int rsrp;

    @ColumnInfo(name = "rssi")
    private int rssi;

    @ColumnInfo(name = "sinr")
    private int sinr;

    @ColumnInfo(name = "rsrq")
    private int rsrq;

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getSinr() {
        return sinr;
    }

    public void setSinr(int sinr) {
        this.sinr = sinr;
    }

    public int getRsrq() {
        return rsrq;
    }

    public void setRsrq(int rsrq) {
        this.rsrq = rsrq;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getTac() {
        return tac;
    }

    public void setTac(int tac) {
        this.tac = tac;
    }

}

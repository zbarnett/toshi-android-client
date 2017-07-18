package com.toshi.model.local;

public class Network {
    private final String id;
    private final String name;
    private final String url;

    public Network(final String networkDescription) {
        final String[] splitString = networkDescription.split("\\|");

        if (splitString.length != 3) throw new IllegalArgumentException("Unexpected network format");

        this.url = splitString[0];
        this.name = splitString[1];
        this.id = splitString[2];
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
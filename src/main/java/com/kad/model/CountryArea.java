package com.kad.model;

public class CountryArea {
    private String id;          // character varying(2)
    private String name;        // text
    private AreaType areaType;  // area_type
    private String parentId;    // character varying(2), nullable

    // Constructeur
    public CountryArea(String id, String name, AreaType areaType, String parentId) {
        this.id = id;
        this.name = name;
        this.areaType = areaType;
        this.parentId = parentId;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AreaType getAreaType() { return areaType; }
    public void setAreaType(AreaType areaType) { this.areaType = areaType; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "CountryArea [id=" + id + ", name=" + name + ", areaType=" + areaType + ", parentId=" + parentId + "]";
    }
}

package com.example.EcoRadar.model.entity;

import com.example.EcoRadar.model.enums.GreenAreaStatus;
import com.example.EcoRadar.model.enums.GreenAreaType;
import com.example.EcoRadar.model.enums.GreenAreaAmenity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "green_areas")
public class GreenArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "green_area_id")
    private Integer id;

    @Column(name = "green_area_name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    private GreenAreaType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "opening_hours", length = 500)
    private String openingHours;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(length = 500)
    private String website;

    @Column(name = "visit_tips", columnDefinition = "TEXT")
    private String visitTips;

    @Column(name = "image_url_1", length = 1000)
    private String imageUrl1;

    @Column(name = "image_url_2", length = 1000)
    private String imageUrl2;

    @Column(name = "image_url_3", length = 1000)
    private String imageUrl3;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "green_area_amenities", joinColumns = @JoinColumn(name = "green_area_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", length = 60)
    private Set<GreenAreaAmenity> amenities = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    private GreenAreaStatus status;

    @OneToOne(mappedBy = "greenArea", cascade = CascadeType.ALL, orphanRemoval = true)
    private GreenAreaAddress address;

    @OneToMany(mappedBy = "greenArea")
    private List<Event> events;

    public GreenArea() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GreenAreaType getType() {
        return type;
    }

    public void setType(GreenAreaType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getVisitTips() {
        return visitTips;
    }

    public void setVisitTips(String visitTips) {
        this.visitTips = visitTips;
    }

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public Set<GreenAreaAmenity> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<GreenAreaAmenity> amenities) {
        this.amenities = amenities != null ? amenities : new LinkedHashSet<>();
    }

    @Transient
    public List<String> getPhotoUrls() {
        List<String> photos = new ArrayList<>();
        if (imageUrl1 != null && !imageUrl1.isBlank()) photos.add(imageUrl1);
        if (imageUrl2 != null && !imageUrl2.isBlank()) photos.add(imageUrl2);
        if (imageUrl3 != null && !imageUrl3.isBlank()) photos.add(imageUrl3);
        return photos;
    }

    @Transient
    public String getPrimaryPhotoUrl() {
        return getPhotoUrls().stream().findFirst().orElse(null);
    }

    public GreenAreaStatus getStatus() {
        return status;
    }

    public void setStatus(GreenAreaStatus status) {
        this.status = status;
    }

    public GreenAreaAddress getAddress() {
        return address;
    }

    public void setAddress(GreenAreaAddress address) {
        this.address = address;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}

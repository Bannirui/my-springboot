package com.github.bannirui.msb.web.entity;

public class PermissionApplyGuide implements Serializable {
    private static final long serialVersionUID = -2858385649457114273L;
    private String image;
    private String title;
    private String message;
    private String description;
    @JSONField(
        name = "apply_hyperlink"
    )
    private String applyHyperlink;
    private List<Informant> informant;

    public PermissionApplyGuide() {
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplyHyperlink() {
        return this.applyHyperlink;
    }

    public void setApplyHyperlink(String applyHyperlink) {
        this.applyHyperlink = applyHyperlink;
    }

    public List<Informant> getInformant() {
        return this.informant;
    }

    public void setInformant(List<Informant> informant) {
        this.informant = informant;
    }
}

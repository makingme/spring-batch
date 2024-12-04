package org.kkb.model;

public class FoodStore {
    private Long seq;
    private String storeName;
    private String storeNo;
    private String storeCall;

    public Long getSeq() { return seq; }
    public void setSeq(Long seq) { this.seq = seq; }

    public String getStoreCall() { return storeCall; }
    public void setStoreCall(String storeCall) { this.storeCall = storeCall; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreNo() { return storeNo; }
    public void setStoreNo(String storeNo) { this.storeNo = storeNo; }
}

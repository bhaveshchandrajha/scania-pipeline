/**
 * Domain entity or value object for the warranty claims model.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.domain;

import java.util.HashMap;
import java.util.Map;

public class SubfileContext {
    
    private String pakz; // @rpg-trace: n428
    private Integer filtag; // @rpg-trace: n439
    private String filart; // @rpg-trace: n447
    private String filoff; // @rpg-trace: n452
    private String filpkz; // @rpg-trace: n504
    private String filtfg; // @rpg-trace: n505
    private String filtkd; // @rpg-trace: n506
    private String filsde; // @rpg-trace: n507
    private String bts010; // @rpg-trace: n504
    private String status; // @rpg-trace: n490
    private String zeichen; // @rpg-trace: n488
    private String suchen; // @rpg-trace: n498
    private String neucla; // @rpg-trace: n529
    private Integer zl1; // @rpg-trace: n424
    private Integer zl2; // @rpg-trace: n424
    private Integer zl4; // @rpg-trace: n405
    private String sub15x; // @rpg-trace: n405
    private String mark11; // @rpg-trace: n408
    private String mark12; // @rpg-trace: n408
    private String mark21; // @rpg-trace: n416
    private String mark22; // @rpg-trace: n416
    private String sub010; // @rpg-trace: n414
    private String sub01x; // @rpg-trace: n414
    private Integer rec1; // @rpg-trace: n533
    private Integer pag1; // @rpg-trace: n533
    private Map<Integer, Boolean> indicators; // @rpg-trace: n405
    
    public SubfileContext() {
        this.indicators = new HashMap<>(); // @rpg-trace: n405
        this.zl1 = 0; // @rpg-trace: n424
        this.zl2 = 0; // @rpg-trace: n424
        this.zl4 = 0; // @rpg-trace: n405
        this.filtag = 0; // @rpg-trace: n439
    }
    
    public void setIndicator(int number, boolean value) { // @rpg-trace: n405
        indicators.put(number, value); // @rpg-trace: n405
    }
    
    public boolean isIndicator(int number) { // @rpg-trace: n428
        return indicators.getOrDefault(number, false); // @rpg-trace: n428
    }
    
    public void clearIndicators(int... numbers) { // @rpg-trace: n405
        for (int num : numbers) { // @rpg-trace: n405
            indicators.put(num, false); // @rpg-trace: n405
        }
    }
    
    public void incrementZl1() { // @rpg-trace: n509
        this.zl1++; // @rpg-trace: n509
    }
    
    public void incrementZl2() { // @rpg-trace: n510
        this.zl2++; // @rpg-trace: n510
    }
    
    // Getters and Setters
    public String getPakz() { return pakz; }
    public void setPakz(String pakz) { this.pakz = pakz; }
    public Integer getFiltag() { return filtag; }
    public void setFiltag(Integer filtag) { this.filtag = filtag; }
    public String getFilart() { return filart; }
    public void setFilart(String filart) { this.filart = filart; }
    public String getFiloff() { return filoff; }
    public void setFiloff(String filoff) { this.filoff = filoff; }
    public String getFilpkz() { return filpkz; }
    public void setFilpkz(String filpkz) { this.filpkz = filpkz; }
    public String getFiltfg() { return filtfg; }
    public void setFiltfg(String filtfg) { this.filtfg = filtfg; }
    public String getFiltkd() { return filtkd; }
    public void setFiltkd(String filtkd) { this.filtkd = filtkd; }
    public String getFilsde() { return filsde; }
    public void setFilsde(String filsde) { this.filsde = filsde; }
    public String getBts010() { return bts010; }
    public void setBts010(String bts010) { this.bts010 = bts010; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getZeichen() { return zeichen; }
    public void setZeichen(String zeichen) { this.zeichen = zeichen; }
    public String getSuchen() { return suchen; }
    public void setSuchen(String suchen) { this.suchen = suchen; }
    public String getNeucla() { return neucla; }
    public void setNeucla(String neucla) { this.neucla = neucla; }
    public Integer getZl1() { return zl1; }
    public void setZl1(Integer zl1) { this.zl1 = zl1; }
    public Integer getZl2() { return zl2; }
    public void setZl2(Integer zl2) { this.zl2 = zl2; }
    public Integer getZl4() { return zl4; }
    public void setZl4(Integer zl4) { this.zl4 = zl4; }
    public String getSub15x() { return sub15x; }
    public void setSub15x(String sub15x) { this.sub15x = sub15x; }
    public String getMark11() { return mark11; }
    public void setMark11(String mark11) { this.mark11 = mark11; }
    public String getMark12() { return mark12; }
    public void setMark12(String mark12) { this.mark12 = mark12; }
    public String getMark21() { return mark21; }
    public void setMark21(String mark21) { this.mark21 = mark21; }
    public String getMark22() { return mark22; }
    public void setMark22(String mark22) { this.mark22 = mark22; }
    public String getSub010() { return sub010; }
    public void setSub010(String sub010) { this.sub010 = sub010; }
    public String getSub01x() { return sub01x; }
    public void setSub01x(String sub01x) { this.sub01x = sub01x; }
    public Integer getRec1() { return rec1; }
    public void setRec1(Integer rec1) { this.rec1 = rec1; }
    public Integer getPag1() { return pag1; }
    public void setPag1(Integer pag1) { this.pag1 = pag1; }
}
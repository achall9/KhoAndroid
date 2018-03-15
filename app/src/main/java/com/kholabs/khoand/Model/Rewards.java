package com.kholabs.khoand.Model;

import com.parse.ParseObject;

/**
 * Created by Aladar-PC2 on 2/2/2018.
 */

public class Rewards {
    public boolean weekly;

    private String identifier;
    private String title;
    private String description;
    private String statsLabel;
    private float rewardOne;
    private float rewardTwo;
    private float rewardThree;
    private int progress;
    private ParseObject origData;

    public void setIdentifier(String _data) { this.identifier = _data; }
    public void setTitle(String _data) { this.title = _data; }
    public void setDescription(String _data) { this.description = _data; }
    public void setStatsLabel(String _data) { this.statsLabel = _data; }
    public void setRewardOne(float _data) { this.rewardOne = _data; }
    public void setRewardTwo(float _data) { this.rewardTwo = _data; }
    public void setRewardThree(float _data) { this.rewardThree = _data; }
    public void setProgress(int _data) { this.progress = _data; }
    public void setOrigData(ParseObject _data) { this.origData = _data; }

    public String getIdentifier() {return identifier;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getStatsLabel() {return statsLabel;}
    public float getRewardOne() {return rewardOne;}
    public float getRewardTwo() {return rewardTwo;}
    public float getRewardThree() {return rewardThree;}
    public int getProgress() {return progress;}
    public ParseObject getOrigData() {return origData;}
}

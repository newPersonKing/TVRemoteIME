package xllib;

/**
 * Created by kingt on 2018/2/2.
 */

public class PlayListItem {
    private String name;
    private int index;
    private long size;
    public PlayListItem(){}
    public PlayListItem(String name, int index, long size){
        this.setName(name);
        this.setIndex(index);
        this.setSize(size);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}

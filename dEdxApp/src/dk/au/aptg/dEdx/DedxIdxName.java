package dk.au.aptg.dEdx;

import java.util.LinkedList;
import java.util.List;

public class DedxIdxName {
    private int idx;
    private String name;
    
    public int getIdx() {
        return idx;
    }
    public void setIdx(int idx) {
        this.idx = idx;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    List<DedxIdxName> getPrograms()
    {
    	return new LinkedList<DedxIdxName>();
    }
    
	public DedxIdxName(int idx, String name) {
		super();
		this.idx = idx;
		this.name = name;
	}
}
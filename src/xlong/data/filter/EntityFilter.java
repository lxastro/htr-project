package xlong.data.filter;

import xlong.data.Entity;

public abstract class EntityFilter {
	EntityFilter father;
	
	public EntityFilter (EntityFilter father){
		this.father = father;
	}
	
	abstract public boolean metaFilter(Entity en);
	
	public boolean filter(Entity en) {
		if (father == null) {
			return metaFilter(en);
		} else {
			return metaFilter(en) && father.filter(en);
		}
	}
}

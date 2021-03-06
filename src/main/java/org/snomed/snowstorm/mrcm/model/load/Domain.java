package org.snomed.snowstorm.mrcm.model.load;

import org.snomed.snowstorm.mrcm.model.InclusionType;

public class Domain {
	private Long conceptId;
	private InclusionType inclusionType;

	public Long getConceptId() {
		return conceptId;
	}

	public void setConceptId(Long conceptId) {
		this.conceptId = conceptId;
	}

	public InclusionType getInclusionType() {
		return inclusionType;
	}

	public void setInclusionType(InclusionType inclusionType) {
		this.inclusionType = inclusionType;
	}
}

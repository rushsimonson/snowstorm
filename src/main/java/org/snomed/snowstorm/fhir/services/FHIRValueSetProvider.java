package org.snomed.snowstorm.fhir.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.snowstorm.core.data.domain.*;
import org.snomed.snowstorm.core.data.services.ConceptService;
import org.snomed.snowstorm.core.data.services.QueryService;
import org.snomed.snowstorm.fhir.config.FHIRConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;

@Component
public class FHIRValueSetProvider implements IResourceProvider, FHIRConstants {
	
	@Autowired
	private QueryService queryService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private HapiValueSetMapper mapper;
	
	@Autowired
	private FHIRHelper fhirHelper;
	
	private static int DEFAULT_PAGESIZE = 1000;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Operation(name="$expand", idempotent=true)
	public ValueSet expand(
			HttpServletRequest request,
			HttpServletResponse response,
			@OperationParam(name="url") String url,
			@OperationParam(name="filter") String filter,
			@OperationParam(name="activeOnly") BooleanType activeType,
			@OperationParam(name="includeDesignations") BooleanType includeDesignationsType,
			@OperationParam(name="designation") List<String> designations,
			@OperationParam(name="displayLanguage") String displayLanguage,
			@OperationParam(name="offset") String offsetStr,
			@OperationParam(name="count") String countStr) throws FHIROperationException {

		//The code system is the URL up to where the parameters start eg http://snomed.info/sct?fhir_vs=ecl/ or http://snomed.info/sct/45991000052106?fhir_vs=ecl/
		int cutPoint = url == null ? -1 : url.indexOf("?");
		if (cutPoint == -1) {
			throw new FHIROperationException(IssueType.VALUE, "'url' parameter is expected to be present, containing eg http://snomed.info/sct?fhir_vs=ecl/ or http://snomed.info/sct/45991000052106?fhir_vs=ecl/ ");
		}
		StringType codeSystemVersionUri = new StringType(url.substring(0, cutPoint));
		String branchPath = fhirHelper.getBranchPathForCodeSystemVersion(codeSystemVersionUri);
		QueryService.ConceptQueryBuilder queryBuilder = queryService.createQueryBuilder(false);  //Inferred view only for now
		
		String ecl = determineEcl(url);
		List<String> languageCodes = fhirHelper.getLanguageCodes(designations, request);
		Boolean active = activeType == null ? null : activeType.booleanValue();
		Boolean includeDesignations = includeDesignationsType == null ? false : includeDesignationsType.booleanValue();
		//If someone specified designations, then include them in any event
		if (designations != null) {
			includeDesignations = true;
		}
		//Also if displayLanguage has been used, ensure that's part of our requested Language Codes
		if (displayLanguage != null && !languageCodes.contains(displayLanguage)) {
			languageCodes.add(displayLanguage);
		}
		
		queryBuilder.ecl(ecl)
					.termMatch(filter)
					.languageCodes(languageCodes)
					.activeFilter(active);

		int offset = (offsetStr == null || offsetStr.isEmpty()) ? 0 : Integer.parseInt(offsetStr);
		int pageSize = (countStr == null || countStr.isEmpty()) ? DEFAULT_PAGESIZE : Integer.parseInt(countStr);
		Page<ConceptMini> conceptMiniPage = queryService.search(queryBuilder, BranchPathUriUtil.decodePath(branchPath), PageRequest.of(offset, pageSize));
		logger.info("Recovered: {} concepts from branch: {} with ecl: '{}'", conceptMiniPage.getContent().size(), branchPath, ecl);
		
		//Do we need further detail on each concept?
		Map<String, Concept> conceptDetails = null;
		if (includeDesignations || displayLanguage != null || designations != null) {
			conceptDetails = getConceptDetailsMap(branchPath, conceptMiniPage, languageCodes);
		}
		
		ValueSet valueSet = mapper.mapToFHIR(conceptMiniPage.getContent(), url, conceptDetails, languageCodes, displayLanguage, includeDesignations); 
		valueSet.getExpansion().setTotal((int)conceptMiniPage.getTotalElements());
		valueSet.getExpansion().setOffset(offset);
		return valueSet;
	}
	
	private Map<String, Concept> getConceptDetailsMap(String branchPath, Page<ConceptMini> page, List<String> languageCodes) {
		Map<String, Concept> conceptDetails = null;
		if (page.hasContent()) {
			conceptDetails = new HashMap<>();
			List<String> ids = page.getContent().stream()
					.map(c -> c.getConceptId())
					.collect(Collectors.toList());
			conceptDetails = conceptService.find(branchPath, ids, languageCodes).stream()
				.collect(Collectors.toMap(Concept::getConceptId, c-> c));
		}
		return conceptDetails;
	}

	/**
	 * See https://www.hl7.org/fhir/snomedct.html#implicit 
	 * @param url
	 * @return
	 * @throws FHIROperationException 
	 */
	private String determineEcl(String url) throws FHIROperationException {
		String ecl;
		if (url.endsWith("?fhir_vs") || url.endsWith("?fhir_vs=refset")) {
			//Return all of SNOMED CT in this situation
			ecl = "*";
		} else if (url.contains(IMPLICIT_ISA)) {
			String sctId = url.substring(url.indexOf(IMPLICIT_ISA) + IMPLICIT_ISA.length());
			ecl = "<<" + sctId;
		} else if (url.contains(IMPLICIT_REFSET)) {
			String sctId = url.substring(url.indexOf(IMPLICIT_REFSET) + IMPLICIT_REFSET.length());
			ecl = "^" + sctId;
		} else if (url.contains(IMPLICIT_ECL)) {
			ecl = url.substring(url.indexOf(IMPLICIT_ECL) + IMPLICIT_ECL.length());
		} else {
			throw new FHIROperationException(IssueType.VALUE, "url is expected to include parameter with value: 'fhir_vs=ecl/'");
		}
		return ecl;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ValueSet.class;
	}
}

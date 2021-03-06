package org.snomed.snowstorm.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.snowstorm.core.data.services.ConceptDefinitionStatusUpdateService;
import org.snomed.snowstorm.core.data.services.SemanticIndexUpdateService;
import org.snomed.snowstorm.core.data.services.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "Admin", description = "-")
@RequestMapping(value = "/admin", produces = "application/json")
public class AdminController {

	@Autowired
	private SemanticIndexUpdateService queryConceptUpdateService;

	@Autowired
	private ConceptDefinitionStatusUpdateService definitionStatusUpdateService;

	@ApiOperation(value = "Rebuild the semantic index of the branch.",
			notes = "You are unlikely to need this action. " +
					"If something has gone wrong with processing of content updates on the branch then semantic index " +
					"which supports the ECL queries can be rebuilt on demand. " +
					"You are unlikely to need this action.")
	@RequestMapping(value = "/{branch}/actions/rebuild-semantic-index", method = RequestMethod.POST)
	public void rebuildBranchTransitiveClosure(@PathVariable String branch) throws ConversionException {
		queryConceptUpdateService.rebuildStatedAndInferredSemanticIndex(BranchPathUriUtil.decodePath(branch));
	}

	@ApiOperation(value = "Force update of definition statuses of all concepts based on axioms.",
			notes = "You are unlikely to need this action. " +
					"If something has wrong with processing content updates on the branch the definition statuses " +
					"of all concepts can be updated based on the concept's axioms. " +
					"You are unlikely to need this action.")
	@RequestMapping(value = "/{branch}/actions/update-definition-statuses", method = RequestMethod.POST)
	@ResponseBody
	public void updateDefinitionStatuses(@PathVariable String branch) throws ServiceException {
		definitionStatusUpdateService.updateAllDefinitionStatuses(BranchPathUriUtil.decodePath(branch));
	}

}

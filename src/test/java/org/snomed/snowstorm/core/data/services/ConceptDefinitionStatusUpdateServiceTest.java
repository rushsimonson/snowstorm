package org.snomed.snowstorm.core.data.services;

import com.google.common.collect.Sets;
import io.kaicode.elasticvc.api.BranchService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snomed.snowstorm.AbstractTest;
import org.snomed.snowstorm.TestConfig;
import org.snomed.snowstorm.core.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.Assert.*;
import static org.snomed.snowstorm.core.data.domain.Concepts.CORE_MODULE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ConceptDefinitionStatusUpdateServiceTest extends AbstractTest {

    private  String path = "MAIN";

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private ReferenceSetMemberService referenceSetMemberService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ConceptDefinitionStatusUpdateService definitionStatusUpdateService;

    @Before
    public void setUp() {
        branchService.create(path);
    }

    @Test
    public void testNewConceptAuthoring() throws ServiceException {
        //create a new concept with class axiom
        final Concept concept = new Concept("50960005", 20020131, true, Concepts.CORE_MODULE, "900000000000074008");
        concept.addAxiom(new Axiom(null, Concepts.FULLY_DEFINED, Sets.newHashSet(new Relationship(Concepts.ISA, "10000100"), new Relationship("10000200", "10000300"))).setModuleId(CORE_MODULE));
        conceptService.create(concept, path);
        assertEquals(1, conceptService.find(concept.getConceptId(), path).getClassAxioms().size());
        final Concept savedConcept = conceptService.find("50960005", path);
        Assert.assertNotNull(savedConcept);
        assertEquals(1, savedConcept.getClassAxioms().size());
        Axiom axiom = savedConcept.getClassAxioms().iterator().next();
        assertEquals(Concepts.FULLY_DEFINED, axiom.getDefinitionStatusId());
        assertEquals("Concept and class axiom should have the same definition status", axiom.getDefinitionStatusId(), savedConcept.getDefinitionStatusId());

        Page<ReferenceSetMember> members = referenceSetMemberService.findMembers(path, true, Concepts.OWL_AXIOM_REFERENCE_SET, savedConcept.getConceptId(), null, null, PageRequest.of(0, 10));
        assertEquals(1, members.getTotalElements());
        String axiomId = axiom.getAxiomId();
        ReferenceSetMember referenceSetMember = members.getContent().stream().filter(member -> member.getMemberId().equals(axiomId)).collect(Collectors.toList()).get(0);
        assertEquals("EquivalentClasses(:50960005 ObjectIntersectionOf(:10000100 ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:10000200 :10000300))) )",
                referenceSetMember.getAdditionalField(ReferenceSetMember.OwlExpressionFields.OWL_EXPRESSION));
    }

    @Test
    public void testAxiomRefsetMemberUpdate() throws ServiceException {
        //create new concept with class axiom
        final Concept concept = new Concept("50960005", 20020131, true, Concepts.CORE_MODULE, Concepts.FULLY_DEFINED);
        concept.addAxiom(new Axiom(null, Concepts.FULLY_DEFINED, Sets.newHashSet(new Relationship(Concepts.ISA, "10000100"), new Relationship("10000200", "10000300"))).setModuleId(CORE_MODULE));
        conceptService.create(concept, path);

        Concept savedConcept = conceptService.find("50960005", path);
        assertEquals(1, savedConcept.getClassAxioms().size());
        assertEquals(Concepts.FULLY_DEFINED, savedConcept.getDefinitionStatusId());

        Axiom axiom = savedConcept.getClassAxioms().iterator().next();
        Page<ReferenceSetMember> members = referenceSetMemberService.findMembers(path, true, Concepts.OWL_AXIOM_REFERENCE_SET, savedConcept.getConceptId(), null, null, PageRequest.of(0, 10));
        assertEquals(1, members.getTotalElements());
        String axiomId = axiom.getAxiomId();
        ReferenceSetMember referenceSetMember = members.getContent().stream().filter(member -> member.getMemberId().equals(axiomId)).collect(Collectors.toList()).get(0);
        assertEquals("EquivalentClasses(:50960005 ObjectIntersectionOf(:10000100 ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:10000200 :10000300))) )",
                referenceSetMember.getAdditionalField(ReferenceSetMember.OwlExpressionFields.OWL_EXPRESSION));
        //No update method yet but will delete existing and create new one
        referenceSetMemberService.deleteMember(path, referenceSetMember.getMemberId());
        //create a SubClass axiom
        ReferenceSetMember updatedRefsetMember = referenceSetMember;
        updatedRefsetMember.setAdditionalField("owlExpression","SubClassOf(:50960005 ObjectIntersectionOf(:10000100 ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:10000200 :10000300))) )");
        updatedRefsetMember.setMemberId(UUID.randomUUID().toString());
        referenceSetMemberService.createMember(path, updatedRefsetMember);

        Concept updatedConcept = conceptService.find(concept.getConceptId(), path);
        assertEquals("900000000000074008", updatedConcept.getDefinitionStatusId());
        assertEquals(1, updatedConcept.getClassAxioms().size());
        Axiom updatedAxiom = updatedConcept.getClassAxioms().iterator().next();
        assertEquals("900000000000074008", updatedAxiom.getDefinitionStatusId());
    }

    @Test
    public void testClassAxiomRefsetDeletion() throws ServiceException {
        //create a new concept with class axiom
        final Concept concept = new Concept("50960005", 20020131, true, Concepts.CORE_MODULE, Concepts.FULLY_DEFINED);
        concept.addAxiom(new Axiom(null, Concepts.FULLY_DEFINED, Sets.newHashSet(new Relationship(Concepts.ISA, "10000100"), new Relationship("10000200", "10000300"))).setModuleId(CORE_MODULE));
        conceptService.create(concept, path);

        Concept savedConcept = conceptService.find("50960005", path);
        assertEquals(1, savedConcept.getClassAxioms().size());
        assertEquals(Concepts.FULLY_DEFINED, savedConcept.getDefinitionStatusId());

        Axiom axiom = savedConcept.getClassAxioms().iterator().next();
        referenceSetMemberService.deleteMember(path, axiom.getAxiomId());

        Concept updatedConcept = conceptService.find(concept.getConceptId(), path);
        assertEquals("900000000000074008", updatedConcept.getDefinitionStatusId());
    }
}
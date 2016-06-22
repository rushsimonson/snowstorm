package com.kaicube.snomed.elasticsnomed.services;

import com.kaicube.snomed.elasticsnomed.App;
import com.kaicube.snomed.elasticsnomed.domain.Branch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = App.class)
public class BranchServiceTest {

	@Autowired
	private BranchService branchService;

	@Test
	public void testCreateFindBranches() throws Exception {
		Assert.assertNull(branchService.find("MAIN"));

		branchService.create("MAIN");

		final Branch main = branchService.find("MAIN");
		Assert.assertNotNull(main);
		Assert.assertNotNull(main.getFatPath());
		Assert.assertNotNull(main.getId());
		Assert.assertNull(main.getBase());
		Assert.assertNotNull(main.getHead());
		Assert.assertEquals("MAIN", main.getFatPath());

		Assert.assertNull(branchService.find("MAIN/A"));
		branchService.create("MAIN/A");
		final Branch a = branchService.find("MAIN/A");
		Assert.assertNotNull(a);
		Assert.assertEquals("MAIN/A", a.getFatPath());

		Assert.assertNotNull(branchService.find("MAIN"));
	}

	@After
	public void tearDown() {
		branchService.deleteAll();
	}
}
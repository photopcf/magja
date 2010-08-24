/**
 *
 */
package com.google.code.magja.service.category;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.code.magja.model.category.Category;
import com.google.code.magja.service.RemoteServiceFactory;

/**
 * @author andre
 *
 */
public class CategoryRemoteServiceTest {

    private CategoryRemoteService service;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        service = new RemoteServiceFactory().getCategoryRemoteService();
    }

    /**
     * Test method for {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#getByIdClean(java.lang.Integer)}.
     */
    @Test
    public void testGetByIdClean() throws Exception {
        Category category = service.getByIdClean(new Integer(2));
        if(category != null) System.out.println(category.toString());
    }

    /**
     * Test method for {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#getByIdWithChildren(java.lang.Integer)}.
     */
    @Test
    public void testGetByIdWithChildren() throws Exception {
        Category category = service.getByIdWithChildren(new Integer(2));
        for (Category child : category.getChildren()) {
            System.out.println(child.toString());
        }
    }

    /**
     * Test method for {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#getByIdWithParent(java.lang.Integer)}.
     */
    @Test
    public void testGetByIdWithParent() throws Exception {
        Category category = service.getByIdWithParent(new Integer(2));
        if (category != null) if (category.getParent() != null) System.out.println(category.getParent().toString());
    }

    /**
     * Test method for {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#getByIdWithParentAndChildren(java.lang.Integer)}.
     */
    @Test
    public void testGetByIdWithParentAndChildren() throws Exception {
        Category category = service.getByIdWithParentAndChildren(new Integer(2));
        if(category != null) {
            if (category.getParent() != null) System.out.println("parent: " + category.getParent().toString());
            if (category.getChildren() != null) {
                System.out.println("children: ");
                for (Category child : category.getChildren()) System.out.println(child.toString());
            }
        }
    }

	/**
	 * Test method for
	 * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#getTree(java.lang.Integer)}.
	 */
	@Test
	public void testGetTree() throws Exception {
		Category category = service.getTree(new Integer(2));
		if(category != null) System.out.println(category.getName());
	}
	
	/**
	 * Test method for search category
	 * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#search(com.google.code.magja.model.category.Category, java.util.ArrayList)}
	 */
	@Test
	public void testSearch() throws Exception {
		Category category = service.getTree(new Integer(2));

		if (category != null) {
			if(category.getChildren() != null) {
				List<String> childrenNames = new ArrayList<String>();
				childrenNames.add(category.getChildren().get(0).getName());
	
				List<Category> categories = service.search(category, childrenNames);
	
				for (Category cat : categories) {
					System.out.println(cat.getId() + ":" + cat.getName());
				}
			}
		}
	}
	
    /**
     * Test method for
     * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#save(com.google.code.magja.model.category.Category)}
     * .
     */
    @Test
    public void testSave() throws Exception {
        Category category = service.getMinimalCategory(service.getDefaultParent().getId(), "Test Category 1");
        service.save(category);

        assertTrue(category.getId() != null);
        
        String newCategoryName = "Test Category 1 (updated)";
        category.setName(newCategoryName);
        service.save(category);
        Category reloadCategory = service.getByIdClean(category.getId());
        assertTrue(reloadCategory.getName().equals(newCategoryName));
    }

    /**
     * Test method for
     * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#delete(java.lang.Integer)}
     */
    @Test
    public void testDelete() throws Exception {
        // first create some category
        Category category = service.getMinimalCategory(service.getDefaultParent().getId(), "Test Category 2");
        int categoryId = service.save(category);

        // then delete it by id
        service.delete(categoryId);
    }

    /**
     * Test method for
     * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#deleteAllChildren(java.lang.Integer)}
     */
    @Test
    public void testDeleteAllChildren() throws Exception {
        service.deleteAllChildren(2);
    }

    /**
     * Test method for
     * {@link com.google.code.magja.service.category.CategoryRemoteServiceImpl#create(java.lang.Integer, java.util.ArrayList)}
     */
    @Test
    public void testCreate() throws Exception {
        Category parent = service.getDefaultParent();

    	List<String> categoryNames = new ArrayList<String>();
    	categoryNames.add("Category 1");
    	categoryNames.add("Sub Category 2");
    	categoryNames.add("Under Sub Category 3");
    	
        service.create(parent.getId(), categoryNames);
    }
}

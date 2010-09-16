/**
 *
 */
package com.google.code.magja.service.category;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import com.google.code.magja.magento.ResourcePath;
import com.google.code.magja.model.category.Category;
import com.google.code.magja.model.product.Product;
import com.google.code.magja.service.GeneralServiceImpl;
import com.google.code.magja.service.ServiceException;

/**
 * @author andre
 *
 */
public class CategoryRemoteServiceImpl extends GeneralServiceImpl<Category> implements CategoryRemoteService {

    /**
     * Load children for the category
     * @param category
     * @throws ServiceException
     */
    private void loadChildren(Category category) throws ServiceException {
        if (category.get("children") != null) {
            if (category.get("children").toString().length() > 0) {
                String str_children = (String) category.get("children");
                String[] arr_children = str_children.split(",");
                for (String str_child : arr_children) {
                    Category child = getByIdClean(new Integer(str_child));
                    if(child != null) category.addChild(child);
                }
            }
        }
    }

    /**
     * load parent for the category
     * @param category
     * @throws ServiceException
     */
    private void loadParent(Category category) throws ServiceException {
        if(category.get("parent_id") != null) {
            Category parent = getByIdClean((Integer) category.get("parent_id"));
            category.setParent(parent);
        }
    }

    /* (non-Javadoc)
     * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdClean(java.lang.Integer)
     */
    @Override
    public Category getByIdClean(Integer id) throws ServiceException {

        Category category = new Category();

        if(id == null) return null;

        Map<String, Object> cat;

        try {
            cat = (Map<String, Object>) soapClient.call(ResourcePath.CategoryInfo, id);
        } catch (AxisFault e) {
        	if(debug) e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }

        if(cat == null) return null;

        for (Map.Entry<String, Object> attribute : cat.entrySet())
            category.set(attribute.getKey(), attribute.getValue());

        return category;
    }

    /* (non-Javadoc)
     * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithChildren(java.lang.Integer)
     */
    @Override
    public Category getByIdWithChildren(Integer id) throws ServiceException {

        Category category = getByIdClean(id);

        // load category children
        loadChildren(category);

        return category;
    }

    /* (non-Javadoc)
     * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithParent(java.lang.Integer)
     */
    @Override
    public Category getByIdWithParent(Integer id) throws ServiceException {

        Category category = getByIdClean(id);

        // load category parent
        loadParent(category);

        return category;
    }

    /* (non-Javadoc)
     * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithParentAndChildren(java.lang.Integer)
     */
    @Override
    public Category getByIdWithParentAndChildren(Integer id) throws ServiceException {

        Category category = getByIdClean(id);

        // load category parent and children
        loadChildren(category);
        loadParent(category);

        return category;
    }
    
	/**
	 * get all category with subcategory by id
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public Category getTree(Integer id) throws ServiceException {
		
		Category category = new Category();

		if (id == null) return null;

		Map<String, Object> cat;

		try {
			cat = (Map<String, Object>) soapClient.call(ResourcePath.CategoryTree, id);
		} catch (AxisFault e) {
			if(debug) e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (cat == null) return null;

		category = getCategoryFromMap(cat);

		return category;
	}

	/**
	 * build category from Map
	 * 
	 * @param Map<String, Object>
	 */
	@SuppressWarnings("unchecked")
	private Category getCategoryFromMap(Map<String, Object> cat) {
		Category category = new Category();

		for (Map.Entry<String, Object> attribute : cat.entrySet()) {
			if (attribute.getKey().equals("children")) {
				List<Category> children = new ArrayList<Category>();

				List<Map<String, Object>> childrenList = (List<Map<String, Object>>) attribute.getValue();

				for (Map<String, Object> child : childrenList) {
					Category c = getCategoryFromMap(child);
					children.add(c);
				}

				category.setChildren(children);
			} else {
				category.set(attribute.getKey(), attribute.getValue());
			}
		}

		return category;
	}
	
	/**
	 * print all categories
	 * 
	 * @param id
	 */
	public void print(Category category) {
		if (category != null) {
			String s = "";
			for (int i = 1; i < category.getLevel(); i++) {
				s += " ";
			}

			System.out.println(s + category.getName());

			for (Category child : category.getChildren()) {
				print(child);
			}
		}
	}
	
	/**
	 * search categories
	 * 
	 * @param id
	 */
	public List<Category> search(Category category, List<String> categoryNames) throws ServiceException {
		List<Category> categories = new ArrayList<Category>();

		for (String name : categoryNames) {
			boolean found = false;
			for (Category child : category.getChildren()) {
				if (child.getName().equals(name)) {
					found = true;
					categories.add(child);

					// override parent with child
					category = child;
					break;
				}
			}

			if (!found) {
				throw new ServiceException("Category \"" + name + "\" not found.");
			}
		}

		return categories;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.code.magja.service.category.CategoryRemoteService#save(code
     * .google .magja.model.product.Category)
     */
    @SuppressWarnings("unchecked")
    @Override
    public int save(Category category) throws ServiceException {
        List<Object> newCategory = (LinkedList<Object>) category.serializeToApi();
        if (category.getId() == null) {
            // means its a new category
            try {
                Integer id = Integer.parseInt((String) soapClient.call(ResourcePath.CategoryCreate, newCategory));
                if (id > -1) {
                    category.setId(id);
                    return id;
                } else {
                    throw new ServiceException("Error inserting new Category");
                }
            } catch (NumberFormatException e) {
            	if(debug) e.printStackTrace();
                throw new ServiceException(e.getMessage());
            } catch (AxisFault e) {
            	if(debug) e.printStackTrace();
                throw new ServiceException(e.getMessage());
            }
        } else {
            // update existing category
            try {
                Boolean sucessed = (Boolean) soapClient.call(ResourcePath.CategoryUpdate, newCategory);
                if (!sucessed) {
                    throw new ServiceException("Error on update Category");
                }
            } catch (AxisFault e) {
            	if(debug) e.printStackTrace();
                throw new ServiceException(e.getMessage());
            }
            return category.getId();
        }
    }

    /**
     * Delete a category by id
     * 
     * @param id
     * @throws ServiceException
     */
    public void delete(Integer id) throws ServiceException {
        Boolean success = false;
        try {
            success = (Boolean) soapClient.call(ResourcePath.CategoryDelete, id);
        } catch (AxisFault e) {
            System.out.println(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        if (!success) {
            throw new ServiceException("Not success deleting category.");
        }
    }

    /**
     * Delete a delete all children in a category
     * 
     * @param parent
     *            category id
     * @throws ServiceException
     */
    public void deleteAllChildren(Integer id) throws ServiceException {
        Category parent = getByIdWithChildren(id);
        List<Category> children = parent.getChildren();

        for (Category category : children) {
            delete(category.getId());
        }
    }

    /**
     * get default root category
     * 
     * @param id
     * @throws ServiceException
     */
    public Category getDefaultParent() throws ServiceException {
        return getByIdClean(soapClient.getConfig().getDefaultRootCategoryId());
    }

    /**
     * create category from minimal parameter
     * 
     * Settings: availableSortBy = name defaultSortBy = name active = true
     * anchor = true
     */
    public Category getMinimalCategory(Integer parentId, String categoryName) {
        return getRequiredCategory(parentId, categoryName, "", "", true, true);
    }

    /**
     * create category with required parameter (this parameter are required by Magento)
     */
    public Category getRequiredCategory(Integer parentId, String categoryName, String availableSortBy, String defaultSortBy, Boolean active, Boolean anchor) {
        Category parent = new Category(parentId);
        
        Category category = new Category(categoryName);
        category.setParent(parent);
        category.setAvailableSortBy(availableSortBy);
        category.setDefaultSortBy(defaultSortBy);
        category.setActive(active);
        category.setAnchor(anchor);
        
        return category;
    }
    
    /**
     * create category tree from String
     */
    public Category create(Integer parentId, String categoryName) throws ServiceException {
    	List<String> categoryNames = new ArrayList<String>();
    	categoryNames.add(categoryName);
    	
        List<Category> categories = create(parentId, categoryNames);
        
        return categories.get(0);
    }

	/**
	 * create category tree from String array
	 */
	public List<Category> create(Integer parentId, List<String> categoryNames) throws ServiceException {
		if (parentId > 0 && categoryNames.size() > 0) {
			List<Category> categories = new ArrayList<Category>();

			Category existingCategories = getTree(parentId);

			List<String> categoryNamesCpy = new ArrayList<String>();
			for (String categoryName : categoryNames) {
				categoryNamesCpy.add(categoryName);

				try {
					categories = search(existingCategories, categoryNamesCpy);
					parentId = categories.get(categories.size() -1).getId();
				} catch (Exception e) {
					Category newCategory = getMinimalCategory(parentId, categoryName);
					parentId = save(newCategory);
					categories.add(newCategory);
				}
			}

			return categories;
		}

		throw new ServiceException("Fail to create a new category");
	}
    
    /**
     * Assign product to category
     */
    public void assignProduct(Category category, Product product) throws ServiceException {
        List<Object> list = new LinkedList<Object>();
        list.add(category.getId());
        list.add(product.getId());

        Boolean success = false;
        try {
            success = (Boolean) soapClient.call(ResourcePath.CategoryAssignProduct, list);
        } catch (AxisFault e) {
            System.out.println(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        if (!success) {
            throw new ServiceException("Not success assign product to category.");
        }
    }
    
    /**
     * Get list of assigned products in default store
     */
    public List<Product> getProducts(Category category) throws ServiceException {
    	return getProducts(category, 1);
    }
    
    /**
     * Get list of assigned products
     */
    public List<Product> getProducts(Category category, Integer storeID) throws ServiceException {

		if (category == null) return null;
		
    	List<Object> list = new LinkedList<Object>();
        list.add(category.getId());
        list.add(storeID);

		List<Product> products = new ArrayList<Product>();

		List<Map<String, Object>> productList;
		
		try {
			productList = (List<Map<String, Object>>) soapClient.call(ResourcePath.CategoryAssignedProducts, list);
		} catch (AxisFault e) {
			if(debug) e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (productList == null)
			return products;

		for (Map<String, Object> mpp : productList) {
			Product product = new Product();
			for (Map.Entry<String, Object> attribute : mpp.entrySet()) {
				product.set(attribute.getKey(), attribute.getValue());
			}
			products.add(product);
		}
		
		return products;
    }
    
    /**
     * get list of last categories in a category tree
     */
    public List<Category> getLastCategories(Category categoryTree) {
        List<Category> categoryList = new ArrayList<Category>();

        for (Category child : categoryTree.getChildren()) {
            if (child.getChildren().isEmpty()) {
                categoryList.add(child);
            } else {
                List<Category> categorys = getLastCategories(child);

                for (Category c : categorys) {
                    categoryList.add(c);
                }
            }
        }

        return categoryList;
    }
    
    /**
     * get list of last categories without products
     */
    public List<Category> findEmpty(Integer id) throws ServiceException {
        Category startCategory = getTree(id);

        List<Category> lastCategories = getLastCategories(startCategory);
        
        List<Category> emptyCategories = new ArrayList<Category>();
        for (Category category : lastCategories) {
            if (getProducts(category).isEmpty()) {
                emptyCategories.add(category);
            }
        }
        
        return emptyCategories;
    }
    
    /**
     * delete last categories without products
     */
    public Integer deleteEmpty(Integer id) throws ServiceException {
        List<Category> emptyCategories = findEmpty(id);
        for (Category category : emptyCategories) {
            delete(category.getId());
        }
        
        // FIXME: delete empty parent
        
        return emptyCategories.size();
    }

}
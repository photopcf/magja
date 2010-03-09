/**
 *
 */
package code.google.magja.model.product;

import code.google.magja.model.BaseMagentoModel;
import code.google.magja.utils.PropertyLoader;

/**
 * @author andre
 *
 */
@SuppressWarnings("serial")
public class ProductType extends BaseMagentoModel {

	private static final String MAPPING_FILE_NAME = "productType-mapping";

	public ProductType(String label, String type) {
		this.label = label;
		this.type = type;
	}

	public ProductType() {
		mapping = PropertyLoader.loadProperties(getClass().getPackage().getName() + "." + MAPPING_FILE_NAME);
	}

	private String label;

	private String type;

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductType other = (ProductType) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProductType [label=" + label + ", type=" + type + "]";
	}

	/**
	 * there is not need to use that method due
	 * to we cannot save ProductType to Magento API
	 * @see code.google.magja.model.BaseMagentoModel#serializeToApi()
	 */
	@Override
	public Object serializeToApi() {
		return null;
	}

}

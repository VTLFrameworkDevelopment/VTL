/*******************************************************************************
 * Copyright 2020, Bank Of Italy
 *
 * Licensed under the EUPL, Version 1.2 (the "License");
 * You may not use this work except in compliance with the
 * License.
 * You may obtain a copy of the License at:
 *
 * https://joinup.ec.europa.eu/sites/default/files/custom-page/attachment/2020-03/EUPL-1.2%20EN.txt
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 *
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
package it.bancaditalia.oss.vtl.impl.domains;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

import it.bancaditalia.oss.vtl.impl.types.data.BaseScalarValue;
import it.bancaditalia.oss.vtl.impl.types.data.StringValue;
import it.bancaditalia.oss.vtl.impl.types.domain.Domains;
import it.bancaditalia.oss.vtl.impl.types.exceptions.VTLCastException;
import it.bancaditalia.oss.vtl.model.data.CodeItem;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.ValueDomain;
import it.bancaditalia.oss.vtl.model.domain.StringCodeListDomain;
import it.bancaditalia.oss.vtl.model.domain.StringDomain;
import it.bancaditalia.oss.vtl.model.domain.StringDomainSubset;

public class StringCodeListDomainImpl implements StringCodeListDomain, Serializable
{
	private static final long serialVersionUID = 1L;

	private final String name; 
	private final Set<StringCodeItemImpl> items = new HashSet<>();
	private final int hashCode;

	public class StringCodeItemImpl extends BaseScalarValue<String, StringCodeListDomain, StringDomain> implements StringCodeItem
	{
		private static final long serialVersionUID = 1L;

		public StringCodeItemImpl(String value)
		{
			super(value, StringCodeListDomainImpl.this);
		}

		@Override
		public int compareTo(ScalarValue<?, ?, ?> o)
		{
			return get().compareTo((String) getDomain().cast(o).get());
		}

		@Override
		public VTLScalarValueMetadata<StringCodeListDomain> getMetadata()
		{
			return this::getDomain; 
		}

		@Override
		public String toString()
		{
			return '"' + get() + '"';
		}
	}
	
	public StringCodeListDomainImpl(String name, Set<? extends String> items)
	{
		this.name = name;
		this.hashCode = 31 + name.hashCode();
		for (String item: items)
			this.items.add(new StringCodeItemImpl(item));
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public StringDomainSubset getDomain()
	{
		return Domains.STRINGDS;
	}

	@Override
	public <T extends Comparable<?> & Serializable> CodeItem<? extends Comparable<?>, StringDomainSubset, StringDomain> getItem(
			ScalarValue<T, StringDomainSubset, StringDomain> value)
	{
		throw new UnsupportedOperationException("getItem");
	}

	@Override
	public Object getCriterion()
	{
		return null;
	}

	@Override
	public StringDomain getParentDomain()
	{
		return Domains.STRINGDS;
	}

	@Override
	public StringCodeItem cast(ScalarValue<?, ?, ?> value)
	{
		if (value instanceof StringCodeItem && items.contains(value))
			return (StringCodeItem) value;
		else if (value instanceof StringValue)
		{
			StringCodeItem item = new StringCodeItemImpl((String) value.get());
			if (items.contains(item))
				return item;
		}

		throw new VTLCastException(this, value);
	}

	@Override
	public boolean isAssignableFrom(ValueDomain other)
	{
		return other instanceof StringCodeListDomain && getVarName().equals(other.getVarName());
	}

	@Override
	public boolean isComparableWith(ValueDomain other)
	{
		return Domains.STRINGDS.isComparableWith(other);
	}

	@Override
	public String getVarName()
	{
		return name + "_var";
	}

	@Override
	public Set<? extends StringCodeItem> getCodeItems()
	{
		return items;
	}
	
	@Override
	public String toString()
	{
		return name + ":" + Domains.STRINGDS;
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringCodeListDomainImpl other = (StringCodeListDomainImpl) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (!items.equals(other.items))
			return false;

		return true;
	}

	@Override
	public StringCodeListDomain trim()
	{
		return stringCodeListHelper("TRIM("+name+")", String::trim);
	}

	@Override
	public StringCodeListDomain ltrim()
	{
		return stringCodeListHelper("LTRIM("+name+")", s -> s.replaceAll("^\\s+",""));
	}

	@Override
	public StringCodeListDomain rtrim()
	{
		return stringCodeListHelper("RTRIM("+name+")", s -> s.replaceAll("\\s+$",""));
	}

	@Override
	public StringCodeListDomain ucase()
	{
		return stringCodeListHelper("UCASE("+name+")", String::toUpperCase);
	}

	@Override
	public StringCodeListDomain lcase()
	{
		return stringCodeListHelper("LCASE("+name+")", String::toLowerCase);
	}

	private StringCodeListDomain stringCodeListHelper(String newName, UnaryOperator<String> mapper)
	{
		return new StringCodeListDomainImpl(newName, items.stream().map(StringCodeItem::get).map(mapper).collect(toSet()));
	}
}

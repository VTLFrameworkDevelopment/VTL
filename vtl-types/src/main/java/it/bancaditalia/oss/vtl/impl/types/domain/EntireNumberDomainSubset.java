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
package it.bancaditalia.oss.vtl.impl.types.domain;

import java.io.Serializable;

import it.bancaditalia.oss.vtl.impl.types.data.DoubleValue;
import it.bancaditalia.oss.vtl.impl.types.data.NullValue;
import it.bancaditalia.oss.vtl.impl.types.exceptions.VTLCastException;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.ValueDomain;
import it.bancaditalia.oss.vtl.model.domain.NumberDomain;
import it.bancaditalia.oss.vtl.model.domain.NumberDomainSubset;

class EntireNumberDomainSubset extends EntireDomainSubset<Double, NumberDomain> implements NumberDomainSubset<NumberDomain>, Serializable
{
	private static final long serialVersionUID = 1L;

	EntireNumberDomainSubset()
	{
		super(Domains.NUMBERDS);
	}

	@Override
	public String toString()
	{
		return "number";
	}
	
	@Override
	public boolean isAssignableFrom(ValueDomain other)
	{
		return other instanceof NumberDomainSubset;
	}

	@Override
	public ScalarValue<?, ? extends NumberDomainSubset<NumberDomain>, ? extends NumberDomain> cast(ScalarValue<?, ?, ?> value)
	{
		if (isAssignableFrom(value.getDomain()))
		{
			if (value instanceof NullValue)
				return NullValue.instance(this);
			Object implValue = value.get();
			if (implValue instanceof Double)
				return new DoubleValue((double)implValue);
			else if (implValue instanceof Long)
				return new DoubleValue((double)(long)implValue);
			else if (implValue instanceof String)
				return new DoubleValue(Double.parseDouble((String)implValue));
			else 
				throw new UnsupportedOperationException("Cast to double from " + value.getClass());
		}
		else 
			throw new VTLCastException(this, value);
	}
}

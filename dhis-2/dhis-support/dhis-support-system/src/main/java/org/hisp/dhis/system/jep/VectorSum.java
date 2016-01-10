package org.hisp.dhis.system.jep;

import java.util.List;
import java.util.Stack;
import java.lang.Object;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.PostfixMathCommandI;

public class VectorSum extends PostfixMathCommand 
implements PostfixMathCommandI 
{
	public VectorSum() {
		numberOfParameters = 1;
	}

	// nFunk's JEP run() method uses the raw Stack type
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run(Stack inStack) throws ParseException {
		// check the stack
		checkStack(inStack);

		Object param= inStack.pop();
		if (param instanceof List) {
			List<Double> vals=CustomFunctions.checkVector(param);
			int n=vals.size();
			if (n==0) {
				inStack.push(new Double(0));
			} else {
				double sum=0; for (Double v: vals) {
					sum=sum+v;}
				inStack.push(new Double(sum));
			}
		}
		else throw new ParseException("Invalid aggregate value in expression");
	}
}


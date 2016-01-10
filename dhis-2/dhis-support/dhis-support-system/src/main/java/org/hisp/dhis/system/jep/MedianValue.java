package org.hisp.dhis.system.jep;

import java.util.List;
import java.util.Stack;
import java.lang.Object;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.PostfixMathCommandI;

public class MedianValue extends PostfixMathCommand implements PostfixMathCommandI {
	public MedianValue() {
		numberOfParameters = 1;
	}

	// nFunk's JEP run() method uses the raw Stack type
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run(Stack inStack) throws ParseException {
		// check the stack
		checkStack(inStack);

		Object param = inStack.pop();
		List<Double> vals = CustomFunctions.checkVector(param);
		int n = vals.size();
		// Sort it here
		if (n % 2 == 0) {
			inStack.push(new Double((vals.get(n / 2) + vals.get(n / 2 + 1)) / 2));
		} else
			inStack.push(new Double(vals.get((n + 1) / 2)));
	}
}

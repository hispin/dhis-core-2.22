package org.hisp.dhis.system.jep;

import java.util.List;
import java.util.Stack;
import java.lang.Object;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;
import org.nfunk.jep.function.PostfixMathCommandI;

public class StandardDeviation extends PostfixMathCommand implements PostfixMathCommandI {
	public StandardDeviation() {
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
		if (n == 0) {
			inStack.push(new Double(0));
		} else {
			double sum = 0, sum2 = 0, mean, variance;
			for (Double v : vals) {
				sum = sum + v;
			}
			;
			mean = sum / n;
			for (Double v : vals) {
				sum2 = sum2 + ((v - mean) * (v - mean));
			}
			variance = sum2 / n;
			inStack.push(new Double(Math.sqrt(variance)));
		}
	}
}

package me.scai.parsetree.evaluation;

import java.util.List;

public class FunctionTerm extends FunctionSigmaPiTerm {
	/* Member variables */
	protected String functionName;

	/* Constructor */
	public FunctionTerm(String tFunctionName, FunctionArgumentList tArgList) {
		super(tArgList);

		functionName = tFunctionName;
	}

	/* Methods */
	/* Getters and setters */
	public String getFunctionName() {
		return functionName;
	}

	/* Evaluation:
	 * @param  evluator:     The instance of ParseTreeEvalautor used in this evaluation operation 
	 * @param  tempArgNaes:  Temporary argument names     
	 * @param  argValueLIst: List of values for the arguments */
	public Object evaluate(ParseTreeEvaluator evaluator, 
	                       List<String> tempArgNames,
	                       FunctionArgumentList argValueList)
			throws ParseTreeEvaluatorException {
		if (!isDefined()) {
			throw new RuntimeException(
					"The body of this function is not defined"); 
			/* TODO: More specific exception type */
		}

		if (!argValueList.allValues()) {
			throw new RuntimeException(
					"Not all items in the argument list are values");
			/* TODO: More specific exception type */
		}

		if (argValueList.numArgs() != argList.numArgs()) {
			throw new RuntimeException("Argument list length mismatch");
			/* TODO: More specific exception type */
		}
		
		if (tempArgNames.size() != argList.numArgs()) {
		    throw new RuntimeException("Argument temporary names length mismatch");
            /* TODO: More specific exception type */
		}
		
		/* "Functionize" the body */
		EvaluatorHelper.functionizeBody(this.evalBody, this.getArgNames());
		
		int numArgs = argList.numArgs();
//		List<String> argSymbols = argList.getSymbolNames();
		List<String> argSymbols = tempArgNames;

		/* Set the values of the arguments, using the temporary variable names */
		for (int i = 0; i < numArgs; ++i) {
			String argSymbol = argSymbols.get(i);
			Object argValue = argValueList.get(i);

			evaluator.variable_assign_value(argSymbol, argValue); 
			/* TODO: Assign matrix-type values */
		}
		
		/* TODO: Replace the symbol node in the body that match argument names with  
		 *       special nodes. */
		
		Object out = evaluator.eval(this.evalBody);
		
		/* Recover argument names */
//		this.setArgNames(origFuncArgNames);

		return out;
	}
	
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("function: ");
		sb.append(functionName);
		sb.append("(");

		int nArgs = argNames.size();
		for (int i = 0; i < nArgs; ++i) {
			sb.append(argNames.get(i));
			if (i < nArgs - 1) {
				sb.append(", ");
			}
		}
		sb.append(")");

		return sb.toString();
	}

}
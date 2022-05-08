package tal;

import static tal.Token.Type.*;

/**
 * Analizador sintáctico implementado mediante el método descendente recursivo.
 * <p>
 * En esta clase se debe implementar la gramática del lenguaje.
 */
public class ADR extends ASin {
	/**
	 * Construye un analizador descendente recursivo.
	 * 
	 * @param lex Analizador léxico.
	 */
	public ADR(ALex lex) {
		super(lex);
	}

	/**
	 * Símbolo inicial de la gramática.
	 */
	public void programa() {
		declaracion();
		bloque();
		tokenRead(EOF);
	}

	private void declaracion() {
		switch (tokenType()) {
		case ENTERO:
			tokenRead(ENTERO);
			tokenRead(ID);
			codeVariableInteger();
			declaracion();
			break;

		case CADENA:
			tokenRead(CADENA);
			tokenRead(ID);
			codeVariableString();
			declaracion();
			break;
		
		}
	}

	private void bloque() {
		switch (tokenType()) {
		case ID:
			asignacion();
			bloque();
			break;

		case IMPRIMIR:
			imprimir();
			bloque();
			break;

		case SI:
			condicion();
			bloque();
			break;
		case MIENTRAS:
			iteracion();
			bloque();
			break;
		}
	}

	private void asignacion() {
		tokenRead(ID);
		codeVariableAssignment();
		tokenRead(ASIGN);
		expresion();
		codeAssignment();
	}

	private void imprimir() {
		tokenRead(IMPRIMIR);
		tokenRead(IPAR);
		expresion();
		tokenRead(DPAR);
		codePrint();
	}

	private void condicion() {
		tokenRead(SI);
		tokenRead(IPAR);
		expresion();
		codeIf();
		tokenRead(DPAR);
		bloque();
		sino();
		tokenRead(FIN);
		codeEnd();
	}

	private void sino() {
		if (tokenType() == SINO) {
			tokenRead(SINO);
			codeElse();
			bloque();
		}
	}

	private void iteracion() {
		tokenRead(MIENTRAS);
		codeWhile();
		tokenRead(IPAR);
		expresion();
		codeIf();
		tokenRead(DPAR);
		bloque();
		tokenRead(FIN);
		codeEnd();
	}

	private void expresion() {
		vor();
		vor1();
	}

	private void vor() {
		vand();
		vand1();
	}

	private void vor1() {
		if (tokenType() == OR) {
			tokenRead(OR);
			vor();
			codeOperator("||");
			vor1();
		}
	}

	private void vand() {
		if (tokenType() == NEG) {
			tokenRead(NEG);
			vrel();
			codeOperator("!");
			vrel1();
		} else {
			vrel();
			vrel1();
		}
	}

	private void vand1() {
		if (tokenType() == AND) {
			tokenRead(AND);
			vand();
			codeOperator("&&");
			vand1();

		}
	}

	private void vrel() {
		if (tokenType() == SUM) {
			boolean negar = "-".equals(tokenName());
			tokenRead(SUM);
			vsum();

			if (negar)
				codeOperator("-1");
			
			vsum1();
		}else {

			vsum();
			vsum1();
		}
	}

	private void vrel1() {
		if (tokenType() == REL) {
			String op = tokenName();
			tokenRead(REL);
			vrel();
			codeOperator(op);
		}
	}

	private void vsum() {
		vmul();
		vmul1();
	}

	private void vsum1() {
		if (tokenType() == SUM) {
			String op = tokenName();
			tokenRead(SUM);
			vsum();
			codeOperator(op);
			vsum1();
		}
	}

	private void vmul() {
		if (tokenType() == IPAR) {
			tokenRead(IPAR);
			expresion();
			tokenRead(DPAR);
		} else {
			valor();
		}

	}

	private void vmul1() {
		if (tokenType() == MUL) {
			String op = tokenName();
			tokenRead(MUL);
			vmul();
			codeOperator(op);
			vmul1();
		}
	}

	private void valor() {
		switch (tokenType()) {
		case ID:
			tokenRead(ID);
			codeVariableExpression();
			break;

		case INTVAL:
			tokenRead(INTVAL);
			codeInteger();
			break;
		default:
			tokenRead(STRVAL);
			codeString();
			break;
		}
	}

}// ADR

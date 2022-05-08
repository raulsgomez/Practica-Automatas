package tal;

import java.util.*;

/**
 * Esta clase se utiliza en <code>ASin</code> para la generación
 * de código.
 */
public class Code
{
private static enum Type
{
    VOID, INTEGER, STRING, BOOLEAN
}

private static enum Action
{
    DECLARE, ASSIGN, PRINT, IF, ELSE, WHILE,
    GOTO, END, VARIABLE, CONSTANT, ADD, SUBTRACT, NEGATE,
    MULTIPLY, DIVIDE, EQUAL, UNEQUAL, LESS, LESSEQ,
    GREATER, GREATEREQ, NOT, OR, AND
}

private static class Variable
{
    private String name;
    private Type   type;
    private Object value;
}

private static class Command
{
    private int line, row, column;
    private Action  action;
    private Type    type;
    private Object  value;
    private Command next;
}

private static class Node
{
    private Type   type;
    private Object value;
}

private interface RunCommand
{
    Command run(Command c);
}

private final RunCommand m_run[] = new RunCommand[Action.values().length];

// Datos de compilacion.
private Token m_token;

// Datos de compilacion y ejecucion
private final Map<String,Variable> m_variables = new HashMap<>();
private Command m_first, m_last;

// Pila para implementar los saltos en condiciones y bucles
private final LinkedList<Command> m_control = new LinkedList<>();

// Pila de ejecucion
private final ArrayDeque<Node> m_stack = new ArrayDeque<>();

//------------------------------------------------------------------------
private void add(Command nodo)
{
    nodo.line   = m_last==null ? 0 : m_last.line + 1;
    nodo.row    = m_token.row;
    nodo.column = m_token.column;

    if(m_first == null)
    {
        assert m_last == null;
        m_first = m_last = nodo;
    }
    else
    {
        assert m_last.next == null;
        m_last.next = nodo;
        m_last = nodo;
    }
}

//------------------------------------------------------------------------
private void error(String mensaje)
{
    throw new RuntimeException(
        "Error ("+ m_token.row +":"+ m_token.column +
        "): "+ mensaje +" "+ m_token.name);
}

//------------------------------------------------------------------------
private Command newCommand(Action accion, Type tipo, Object value)
{
    Command c = new Command();
    c.row    = m_token.row;
    c.column = m_token.column;
    c.action = accion;
    c.type   = tipo;
    c.value  = value;
    return c;
}

//------------------------------------------------------------------------
private void declareVariable(Type tipo)
{
    String nombre = m_token.name;

    if(m_variables.containsKey(nombre))
        error("Ya existe la variable");

    Variable v = new Variable();
    v.name   = nombre;
    v.type     = tipo;
    v.value    = tipo == Type.STRING ? "" : 0;
    m_variables.put(nombre, v);

    add(newCommand(Action.DECLARE, tipo, v));
}

//------------------------------------------------------------------------
public void declareVariableInteger(Token token)
{
    m_token = token;
    declareVariable(Type.INTEGER);
}

//------------------------------------------------------------------------
public void declareVariableString(Token token)
{
    m_token = token;
    declareVariable(Type.STRING);
}

//------------------------------------------------------------------------
public void addAssignment(Token token)
{
    m_token = token;
    add(newCommand(Action.ASSIGN, Type.VOID, null));
}

//------------------------------------------------------------------------
public void addPrint(Token token)
{
    m_token = token;
    add(newCommand(Action.PRINT, Type.VOID, null));
}

//------------------------------------------------------------------------
private void pushCtrl(Command n)
{
    m_control.addFirst(n);
}

//------------------------------------------------------------------------
private Command popCtrl()
{
    if(m_control.isEmpty())
        throw new RuntimeException("Pila vacia.");

    return m_control.removeFirst();
}

//------------------------------------------------------------------------
public void addIf(Token token)
{
    m_token = token;
    Command c = newCommand(Action.IF, Type.VOID, null);
    add(c);
    pushCtrl(c);
}

//------------------------------------------------------------------------
public void addElse(Token token)
{
    m_token = token;
    Command gotoEnd = newCommand(Action.GOTO, Type.VOID, null);
    add(gotoEnd);

    Command nodoElse = newCommand(Action.ELSE, Type.VOID, null);
    add(nodoElse);

    popCtrl().value = nodoElse;
    pushCtrl(gotoEnd);
}

//------------------------------------------------------------------------
public void addWhile(Token token)
{
    m_token = token;
    Command c = newCommand(Action.WHILE, Type.VOID, null);
    add(c);
    pushCtrl(c);
}

//------------------------------------------------------------------------
public void addEnd(Token token)
{
    m_token = token;
    Command gotoInicio = null;

    boolean bucle = m_control.size() >= 2 &&
                    m_control.get(1).action == Action.WHILE;
    if(bucle)
    {
        gotoInicio = newCommand(Action.GOTO, Type.VOID, null);
        add(gotoInicio);
    }

    Command fin = newCommand(Action.END, Type.VOID, null);
    add(fin);

    // GOTO al final del bloque: cuando no se cumpla la condicion.
    popCtrl().value = fin;

    if(bucle)
    {
        // GOTO al inicio del bucle.
        gotoInicio.value = popCtrl();
    }
}

//------------------------------------------------------------------------
public void addVariableAssignment(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
    {
        throw new RuntimeException(
            "No existe la variable: "+ token.name);
    }

    add(newCommand(Action.VARIABLE, Type.VOID, v));
}

//------------------------------------------------------------------------
public void addVariableExpression(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
    {
        throw new RuntimeException(
            "No existe la variable: "+ token.name);
    }

    add(newCommand(Action.VARIABLE, v.type, v));
}

//------------------------------------------------------------------------
public void addInteger(Token token)
{
    m_token = token;
    Object valor = Long.parseLong(token.name);
    add(newCommand(Action.CONSTANT, Type.INTEGER, valor));
}

//------------------------------------------------------------------------
public void addString(Token token)
{
    m_token = token;
    add(newCommand(Action.CONSTANT, Type.STRING, token.name));
}

//------------------------------------------------------------------------
public void addOperator(String operator)
{
    Action action;

    switch(operator)
    {
        case "+":  action = Action.ADD;       break;
        case "-":  action = Action.SUBTRACT;  break;
        case "-1": action = Action.NEGATE;    break;
        case "*":  action = Action.MULTIPLY;  break;
        case "/":  action = Action.DIVIDE;    break;
        case "==": action = Action.EQUAL;     break;
        case "!=": action = Action.UNEQUAL;   break;
        case "<":  action = Action.LESS;      break;
        case "<=": action = Action.LESSEQ;    break;
        case ">":  action = Action.GREATER;   break;
        case ">=": action = Action.GREATEREQ; break;
        case "!":  action = Action.NOT;       break;
        case "||": action = Action.OR;        break;
        case "&&": action = Action.AND;       break;
        default: throw new IllegalArgumentException(operator);
    }

    add(newCommand(action, Type.VOID, null));
}

//------------------------------------------------------------------------
@Override public String toString()
{
    StringBuilder s = new StringBuilder();
    Command n = m_first;

    while(n != null)
    {
        s.append(String.format("%5d:  ", n.line));

        Type tipo = n.value instanceof Variable ?
                      ((Variable)n.value).type : n.type;
        switch(tipo)
        {
            case VOID:    s.append("     "); break;
            case INTEGER: s.append("int  "); break;
            case STRING:  s.append("str  "); break;
            case BOOLEAN: s.append("bool "); break;
            default: throw new AssertionError();
        }

        switch(n.action)
        {
            case DECLARE:   s.append("decl  "); break;
            case ASSIGN:    s.append(":=    "); break;
            case PRINT:     s.append("print "); break;
            case IF:        s.append("if    "); break;
            case ELSE:      s.append("else  "); break;
            case WHILE:     s.append("while "); break;
            case GOTO:      s.append("goto  "); break;
            case END:       s.append("end   "); break;
            case VARIABLE:  s.append("var   "); break;
            case CONSTANT:  s.append("cte   "); break;
            case ADD:       s.append("+     "); break;
            case SUBTRACT:  s.append("-     "); break;
            case NEGATE:    s.append("-1    "); break;
            case MULTIPLY:  s.append("*     "); break;
            case DIVIDE:    s.append("/     "); break;
            case EQUAL:     s.append("==    "); break;
            case UNEQUAL:   s.append("!=    "); break;
            case LESS:      s.append("<     "); break;
            case LESSEQ:    s.append("<=    "); break;
            case GREATER:   s.append(">     "); break;
            case GREATEREQ: s.append("<=    "); break;
            case NOT:       s.append("!     "); break;
            case OR:        s.append("||    "); break;
            case AND:       s.append("&&    "); break;
            default: throw new AssertionError();
        }

        if(n.value instanceof Variable)
            s.append(((Variable)n.value).name);
        else if(n.action == Action.GOTO || n.action == Action.IF)
            s.append(((Command)n.value).line);
        else if(n.type == Type.STRING)
            s.append("\""+ n.value +"\"");
        else if(n.type == Type.INTEGER)
            s.append(n.value);

        s.append("\n");
        n = n.next;
    }

    return s.toString();
}

//------------------------------------------------------------------------
private void stackPush(Type type, Object value)
{
    Node n  = new Node();
    n.type  = type;
    n.value = value;
    m_stack.addFirst(n);
}

//------------------------------------------------------------------------
private Node stackPop()
{
    if(m_stack.isEmpty())
        throw new RuntimeException("Pila vacia.");

    return m_stack.removeFirst();
}

//------------------------------------------------------------------------
private void checkTypes(Command c, Node n1, Type t2)
{
    if(n1.type != t2)
    {
        throw new RuntimeException(
            "Tipos incompatibles en "+ c.row +"."+ c.column);
    }
}

//------------------------------------------------------------------------
private RunCommand newRunNext()
{
    return c -> c.next;
}

//------------------------------------------------------------------------
private RunCommand newRunAssign() { return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    if(n1.type != Type.VOID)
        throw new RuntimeException("No es una variable de asignacion.");

    Variable v = (Variable)n1.value;
    checkTypes(c, n2, v.type);
    v.value = n2.value;
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunPrint() { return c ->
{
    System.out.println(stackPop().value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunIf() { return c ->
{
    Node n = stackPop();
    checkTypes(c, n, Type.BOOLEAN);
    return (Boolean)n.value ? c.next : (Command)c.value;
};}

//------------------------------------------------------------------------
private RunCommand newRunGoto()
{
    return c -> (Command)c.value;
}

//------------------------------------------------------------------------
private RunCommand newRunVariable() {return c ->
{
    Variable v = (Variable)c.value;

    if(c.type == Type.VOID)
    {
        // Variable de asignacion.
        stackPush(c.type, c.value);
    }
    else
    {
        // Variable de expresion.
        assert c.type == v.type;
        stackPush(v.type, v.value);
    }

    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunConstant() {return c ->
{
    stackPush(c.type, c.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunAdd() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    if(n1.type == Type.STRING || n2.type == Type.STRING)
    {
        stackPush(Type.STRING, n1.value +""+ n2.value);
    }
    else
    {
        checkTypes(c, n1, Type.INTEGER);
        checkTypes(c, n2, Type.INTEGER);
        stackPush(Type.INTEGER, (Long)n1.value + (Long)n2.value);
    }

    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunSubtract() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.INTEGER, (Long)n1.value - (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunNegate() {return c ->
{
    Node n = stackPop();
    checkTypes(c, n, Type.INTEGER);
    stackPush(Type.INTEGER, -(Long)n.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunMultiply() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.INTEGER, (Long)n1.value * (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunDivide() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.INTEGER, (Long)n1.value / (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunEqual() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, n1.value.equals(n2.value));
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunUnequal() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, !n1.value.equals(n2.value));
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunLess() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, (Long)n1.value < (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunLessEq() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, (Long)n1.value <= (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunGreater() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, (Long)n1.value > (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunGreaterEq() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.INTEGER);
    checkTypes(c, n2, Type.INTEGER);
    stackPush(Type.BOOLEAN, (Long)n1.value >= (Long)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunNot() {return c ->
{
    Node n = stackPop();
    checkTypes(c, n, Type.BOOLEAN);
    stackPush(Type.BOOLEAN, !(Boolean)n.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunOr() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.BOOLEAN);
    checkTypes(c, n2, Type.BOOLEAN);
    stackPush(Type.BOOLEAN, (Boolean)n1.value || (Boolean)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private RunCommand newRunAnd() {return c ->
{
    Node n2 = stackPop(),
         n1 = stackPop();

    checkTypes(c, n1, Type.BOOLEAN);
    checkTypes(c, n2, Type.BOOLEAN);
    stackPush(Type.BOOLEAN, (Boolean)n1.value && (Boolean)n2.value);
    return c.next;
};}

//------------------------------------------------------------------------
private void inicializarRun()
{
    RunCommand runNext = newRunNext();

    m_run[Action.DECLARE  .ordinal()] = runNext;
    m_run[Action.ASSIGN   .ordinal()] = newRunAssign();
    m_run[Action.PRINT    .ordinal()] = newRunPrint();
    m_run[Action.IF       .ordinal()] = newRunIf();
    m_run[Action.ELSE     .ordinal()] = runNext;
    m_run[Action.WHILE    .ordinal()] = runNext;
    m_run[Action.GOTO     .ordinal()] = newRunGoto();
    m_run[Action.END      .ordinal()] = runNext;
    m_run[Action.VARIABLE .ordinal()] = newRunVariable();
    m_run[Action.CONSTANT .ordinal()] = newRunConstant();
    m_run[Action.ADD      .ordinal()] = newRunAdd();
    m_run[Action.SUBTRACT .ordinal()] = newRunSubtract();
    m_run[Action.NEGATE   .ordinal()] = newRunNegate();
    m_run[Action.MULTIPLY .ordinal()] = newRunMultiply();
    m_run[Action.DIVIDE   .ordinal()] = newRunDivide();
    m_run[Action.EQUAL    .ordinal()] = newRunEqual();
    m_run[Action.UNEQUAL  .ordinal()] = newRunUnequal();
    m_run[Action.LESS     .ordinal()] = newRunLess();
    m_run[Action.LESSEQ   .ordinal()] = newRunLessEq();
    m_run[Action.GREATER  .ordinal()] = newRunGreater();
    m_run[Action.GREATEREQ.ordinal()] = newRunGreaterEq();
    m_run[Action.NOT      .ordinal()] = newRunNot();
    m_run[Action.OR       .ordinal()] = newRunOr();
    m_run[Action.AND      .ordinal()] = newRunAnd();
}

//------------------------------------------------------------------------
public void run()
{
    inicializarRun();
    Command c = m_first;

    while(c != null)
        c = m_run[c.action.ordinal()].run(c);
}

} // Code


/*
 * Copyright (c) under GPL V3. Read LICENSE located in the root of the project.
 * All rights reserved.
 */

package silklang.Interpreter;

import silklang.App.Silk;
import silklang.Environment.Environment;
import silklang.Error.RuntimeError;
import silklang.ParserRepresentation.Expressions.base.Expr;
import silklang.ParserRepresentation.Expressions.base.ExprVisitor;
import silklang.Lexer.Token;
import silklang.ParserRepresentation.Expressions.representations.*;
import silklang.ParserRepresentation.Statement.Representation.Expression;
import silklang.ParserRepresentation.Statement.Representation.Print;
import silklang.ParserRepresentation.Statement.Representation.Var;
import silklang.ParserRepresentation.Statement.base.Stmt;
import silklang.ParserRepresentation.Statement.base.StmtVisitor;

import java.util.List;


public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {

    private Environment env;

    public Interpreter(){
        this.env = new Environment();
    }

    @Deprecated
    public void interpret(Expr expression){
        try{
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        }catch (RuntimeError error){
            Silk.runtimeError(error);
        }
    }

    public void interpret(List<Stmt> statements){

        try{
            for(Stmt st: statements){
                execute(st);
            }
        }catch (RuntimeError error){
            Silk.runtimeError(error);
        }
    }
    private void execute(Stmt st){
        st.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private boolean isTruthy(Object object){
        if (object == null) return false;
        if(object instanceof  Boolean) return(boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b){
        if(a == null && b == null) return true;
        if(a  == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double)return;
        throw new RuntimeError(operator, "El operando debe ser un numero. ");
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double)return;
        throw new RuntimeError(operator, "Los operandos tienen que ser numeros. ");

    }
    private void divisionbyZero(Token operator, Object left, Object right){
        if((double)right!=0)return;
        throw new RuntimeError(operator, "La division por cero no esta permitida.");
    }


    @Override
    public Object visitAssignExpr(Assign expr) {

        Object value = evaluate(expr.getValue());
        env.assign(expr.getName(), value);

        return value;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().getType()){
            case GREATER:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.getOperator(), left, right);
                divisionbyZero(expr.getOperator(), left, right);
                return (double)left / (double) right;
            case STAR:
                checkNumberOperands(expr.getOperator(), left, right);
                return (double)left * (double) right;
            case PLUS:
                if(left instanceof  Double && right instanceof Double){
                    return (double)left+(double)right;
                }
                if(left instanceof String && right instanceof String){
                    return (String)left + (String)right;
                }
                break;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

        }
        return null;
    }

    @Override
    public Object visitCallExpr(Call expr) {
        return null;
    }

    @Override
    public Object visitGetExpr(Get expr) {
        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        return null;
    }

    @Override
    public Object visitSetExpr(Set expr) {
        return null;
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        return null;
    }

    @Override
    public Object visitThisExpr(This expr) {
        return null;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.getRight());
        switch (expr.getOperator().getType()){
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.getOperator(), right);
                return -(double)right;
        }
        //unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return env.get(expr.getName());
    }

    @Override
    public Void visitExpressionStmt(Expression expr) {
        evaluate(expr.getExpr());
        return null;
    }

    @Override
    public Void visitPrintStmt(Print pr) {
        Object value = evaluate(pr.getExpr());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Var vr) {
        Object value = null;
        if(vr.getExpression()!= null){
            value = evaluate(vr.getExpression());
        }
        env.define(vr.getName().getLexeme(), value);
        return null;
    }
}

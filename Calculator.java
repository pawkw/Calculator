// Possible extensions.
// Update: These have been added.
// * Change the program to handle modulo.
// ** Change the program to handle brackets. The definition for factor would be:
//        factor = number | -factor | (expression)

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This is to store our "binary tree". A binary tree is a node that branches
// off into two nodes. A left hand node and a right hand node. Each of these
// nodes can have a left and right hand node as well.
// We are going to use it like this:
// 1+2
//                 +
//                / \
//               1  2
//
// 1+2*3
//                 +
//                / \
//               1  *
//                 / \
//                2  3
//
// Once build the tree, descending down the branches, we start from the bottom
// and "walk the tree" back up to the top. So the node 2*3 is replaced by 6:
//
//                 +
//                / \
//               1  6
//
// And finally, 1+6 = 7.

class Node {
    char operator;
    double data;
    Node leftHandSide;
    Node rightHandSide;

    public Node() {
        this.operator = '\0'; // This means null.
        this.data = 0;
        this.leftHandSide = null;
        this.rightHandSide = null;
    }

    // For example Node node = new Node('d', 5.0);
    public Node(char op, double data) {
        // Not needed, but good practice.
        this();
        this.operator = op;
        this.data = data;
    }

    // For example Node node = new Node('+', new Node('d', 1.0), new Node('d', 2.0));
    public Node(char op, Node lhs, Node rhs) {
        // Not needed, but good practice.
        this();
        this.operator = op;
        this.leftHandSide = lhs;
        this.rightHandSide = rhs;
    }
}

// We need to define an exception for instances where the actual input does not match
// the expected input.
class UnexpectedInputException extends Exception {
    private int cursor;
    private String input;
    private String expectation;
    private char found;

    public UnexpectedInputException(String input, int cursor, String expectation, char found) {
        this.cursor = cursor;
        this.input = input;
        this.expectation = expectation;
        this.found = found;
    }

    public String errorDetail() {
        String error = this.input+'\n'+String.format("%"+(this.cursor+1)+"s", "^");
        error += "\nExpected: "+this.expectation+" Found: "+this.found;
        return error;
    }
}
// We need to use a class here because we have to track what part of the
// expression we are currently looking at AND we have to return nodes.
// The only way to return both values, the node and the current location is
// to have a "cursor" stored in this class.
// Python allows you to return multiple variables, but it is one of only a
// couple of languages that allow it.
class InputHandler{
    String input;
    int cursor; // This stores an index in the String that gets updated.

    public InputHandler(String in) {
        this.input = in.replaceAll("\\s", "");
    }

    public char look() {
        if(cursor < this.input.length())
            return this.input.charAt(cursor);
        else
            return '\0'; // This means null.
    }

    // This is for printing errors and parsing numbers.
    // It returns the remaining string we haven't parsed.
    public String remainder() {
        return this.input.substring(this.cursor);
    }

    public void consume(char edible) throws UnexpectedInputException {
        if(edible != this.input.charAt(this.cursor)) {
            throw new UnexpectedInputException(this.input, this.cursor, String.valueOf(edible), this.look());
        }
        this.cursor++;
        // System.out.println("Consumed: "+edible+" Cursor: "+Integer.toString(this.cursor));
    }

    public void consume(String edible) throws UnexpectedInputException {
        if(!this.input.substring(this.cursor).startsWith(edible)) {
            throw new UnexpectedInputException(this.input, this.cursor, edible, this.look());
        }
        this.cursor += edible.length();
        // System.out.println("Consumed: "+edible+" Cursor: "+Integer.toString(this.cursor));
    }
}

public class Calculator {
    public static void main(String[] args) {
        InputHandler expression;
        String input;
        Scanner keyboardInput = new Scanner(System.in);
        Node breakdown; // For breaking the expression down into its parts.

        // Get the expression from the user.
        System.out.print("Enter and expression to calculate: ");
        input = keyboardInput.nextLine();
        expression = new InputHandler(input);
        System.out.println("Parsing: "+expression.input);
        // Break the expression down.
        breakdown = parseExpression(expression);

        // Print out the breakdown.
        System.out.print("Parsed as: ");
        printTree(breakdown);
        System.out.println();

        // Calculate the answer.
        double answer = calculate(breakdown);
        System.out.println("\nThe answer is "+answer);
    }

    public static void printTree(Node tree) {
        // Here we walk the tree recursively.
        char op = tree.operator;

        // If this is a data node, print it out and return.
        if(op == 'd') {
            System.out.print(tree.data);
            return;
        } else if(op == 'n') {
            System.out.print("(- ");
            printTree(tree.leftHandSide);
            System.out.print(")");
            return;
        }

        // Otherwise, print out the left and right sides.
        System.out.print("(");
        System.out.print(op+" ");
        printTree(tree.leftHandSide);
        System.out.print(" ");
        printTree(tree.rightHandSide);
        System.out.print(")");
    }

    public static double calculate(Node tree) {
        // Walking the tree here is exactly the same as walking the tree
        // in printTree.
        char op = tree.operator;

        // If this is a data node, return the data.
        if(op == 'd') {
            return tree.data;
        } else if(op == 'n') { // If this is a unary minus, return the negative.
            return -calculate(tree.leftHandSide);
        }

        // Otherwise, get the value of the left and right sides.
        double x = calculate(tree.leftHandSide);
        double y =  calculate(tree.rightHandSide);

        // Perform the operation on the value.
        double answer = 0;
        switch(op) {
            case('+'):
                answer = x+y;
                break;
            case('-'):
                answer = x-y;
                break;
            case('*'):
                answer = x*y;
                break;
            case('/'):
                answer = x/y;
                break;
            case('%'):
                answer = x%y;
                break;
        }
        return answer;
    }

/************************************************************************
 * Expressions can be broken down into easier pieces:                   *
 * expression = term [+|- term]*                                        *
 *       term = factor [*|/ factor]*                                    *
 *     factor = number | -number                                        *
 *                                                                      *
 * The asterisk means to do something zero or more times.               *
 * That means we will using a while loop. To do something one or more   *
 * we would use a do-while loop.                                        *
 ************************************************************************/

    // Parse expressions.
    // Given expression, return a "tree" of nodes.
    public static Node parseExpression(InputHandler expression) {
        Node returnNode;

        // Get the term.
        returnNode = parseTerm(expression);

        // Get the operator. If there is an operator, that means we have a
        // right hand side. The term we got above will become the left hand side.
        char op = expression.look();
        while(op == '+' || op == '-') {
            // Update the cursor and "consume" the operator.
            try {
                expression.consume(op);
            } catch (UnexpectedInputException e) {
                System.out.println(e.errorDetail());
            }

            // Get the right hand side.
            returnNode = new Node(op, returnNode, parseTerm(expression));
            op = expression.look();
        }
        return returnNode;
    }

    // Parse terms. Pretty much the same as expressions.
    public static Node parseTerm(InputHandler term) {
        Node returnNode;

        // Get the factor.
        returnNode = parseFactor(term);

        // Get the operator. If there is an operator, that means we have a
        // right hand side. The term we got above will become the left hand side.
        char op = term.look();
        while(op == '*' || op == '/' || op == '%') {
            // Update the cursor and "consume" the operator.
            try {
                term.consume(op);
            } catch (UnexpectedInputException e) {
                System.out.println(e.errorDetail());
            }
            // Get the right hand side.
            returnNode = new Node(op, returnNode, parseFactor(term));
            op = term.look();
        }
        return returnNode;
    }

    // Parse factors.
    // Factors do not have a left or right hand side.
    // The operator is going to be "double" and data is going to be the number.
    public static Node parseFactor(InputHandler factor) {
        Node returnNode = new Node();

        // See if there is a minus sign.
        // Java can parse this for us, but it is better to take care of it
        // ourselves. We can use it later to parse brackets with negatives.
        if(factor.look() == '-') {
            // Update the cursor to "consume" the minus sign.
            try {
                factor.consume('-');
            } catch (UnexpectedInputException e) {
                System.out.println(e.errorDetail());
            }
            // Get the negated factor.
            returnNode.operator = 'n'; // n for negate.
            returnNode.leftHandSide = parseFactor(factor);
            return returnNode;
        } else if(factor.look() == '(') {
            // Update the cursor to "consume" the bracket.
            try {
                factor.consume('(');
            } catch (UnexpectedInputException e) {
                System.out.println(e.errorDetail());
            }
            // The the contents of the bracketed expression.
            returnNode = parseExpression(factor);
            // Check for matching bracket.
            try {
                factor.consume(')');
            } catch (UnexpectedInputException e) {
                System.out.println(e.errorDetail());
            }
            return returnNode;
        }

        // Get the number.
        returnNode.operator = 'd'; // 'd' for double.
        returnNode.data = getNumber(factor);
        return returnNode;
    }

    // Given the substring where the number should begin.
    public static double getNumber(InputHandler numString) {
        // This is a regex expression to recognize numbers with or without
        // a decimal point. We are going to use Java's pattern matching
        // capabilities. The "^" means to only consider stuff at the
        // beginning of the string.
        Pattern doublePattern = Pattern.compile("^([0-9]*[.])?[0-9]+");
        // Apply the pattern to the input starting at the cursor.
        Matcher matchDouble = doublePattern.matcher(numString.remainder());

        float number = 0;
        String getFloat = new String();

        if(matchDouble.find()) {
            getFloat = matchDouble.group();
        } else {
            // A number was not found where it was expected.
            System.out.println("Error retrieving a number: "+numString.remainder());
            System.exit(1);
        }

        // Convert the string to an actual number.
        number = Float.parseFloat(getFloat);
        // Consume the number from the input.
        try {
            numString.consume(getFloat);
        } catch (UnexpectedInputException e) {
            System.out.println(e.errorDetail());
        }

        return number;
    }
}

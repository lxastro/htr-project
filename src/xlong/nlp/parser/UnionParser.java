package xlong.nlp.parser;

public class UnionParser extends Parser {

	private Parser parser1;
	private Parser parser2;
	private String delimiter;
	
	public UnionParser(Parser father, Parser parser1, Parser parser2, String delimiter) {
		super(father);
		this.parser1 = parser1;
		this.parser2 = parser2;
		this.delimiter = delimiter;
	}
	
	public UnionParser(Parser father, Parser parser1, Parser parser2){
		this(father, parser1, parser2, " ");
	}

	@Override
	protected String myParse(String text) {
		return parser1.parse(text) + delimiter + parser2.parse(text);
	}

}

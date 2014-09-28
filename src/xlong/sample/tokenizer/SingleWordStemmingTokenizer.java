package xlong.sample.tokenizer;

import java.util.LinkedList;
import java.util.List;

import weka.core.stemmers.Stemmer;

public class SingleWordStemmingTokenizer extends Tokenizer {

	private static final String DELIMITERSREG = "[0-9_\\W]";
	private static Stemmer stemmer =  new weka.core.stemmers.NullStemmer();;
	private static int mode = 0;

	private static void initStemmer(int newMode) {
		if (mode != newMode) {
			mode = newMode;
			switch (mode) {
			case 0:
				stemmer = new weka.core.stemmers.NullStemmer();
				break;
			case 1:
				stemmer = new weka.core.stemmers.SnowballStemmer();
				break;
			default:
				stemmer = new weka.core.stemmers.PTStemmer();
				break;
			}
		}
	}
	public SingleWordStemmingTokenizer(int mode) {
		super(null);
		initStemmer(mode);
	}

	public SingleWordStemmingTokenizer(int mode, Tokenizer father) {
		super(father);
		initStemmer(mode);
	}

	private List<String> processWords(String[] splitString) {
		LinkedList<String> clean = new LinkedList<String>();

		for (int i = 0; i < splitString.length; i++) {
			String word = splitString[i].trim();
			if (!splitString[i].equals("")) {
				word = stemmer.stem(word);
				clean.add(word);
			}
		}
		return clean;
	}

	@Override
	public List<String> myTokenize(String text) {
		return processWords(text.toLowerCase().split(DELIMITERSREG));
	}

	public static void main(String[] args) {
		String testString = "This is the test string. Test single word stemmer tokenizers. And a URL: http://www.nfl.com/teams/greenbaypackers/profile?team=GB";
		for (String word : new SingleWordStemmingTokenizer(0).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
		for (String word : new SingleWordStemmingTokenizer(1).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
		for (String word : new SingleWordStemmingTokenizer(2).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
	}

}

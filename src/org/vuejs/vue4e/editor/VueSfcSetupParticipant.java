package org.vuejs.vue4e.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class VueSfcSetupParticipant implements IDocumentSetupParticipant {

	private static final String[] PARTITION_TYPES = {
		"TEMPLATE",
		"SCRIPT",
		"STYLE"
	};
	
	@Override
	public void setup(IDocument doc) {
		IDocumentPartitioner partitioner = new FastPartitioner(new VueSfcPartitionScanner(), PARTITION_TYPES);
		partitioner.connect(doc);
		
	}
	
	private class VueSfcPartitionScanner extends RuleBasedPartitionScanner {
	  public VueSfcPartitionScanner() {
  		super();
  		IToken template = new Token(PARTITION_TYPES[0]);
  		IToken script = new Token(PARTITION_TYPES[1]);
  		IToken style = new Token(PARTITION_TYPES[2]);
  
  		List rules = new ArrayList();
  		// TODO: this is too naive
  		rules.add(new MultiLineRule("<template>", "</template>", template, (char) 0, true)); 
  		rules.add(new MultiLineRule("<script>", "</script>", script, (char) 0, true)); 
  		rules.add(new MultiLineRule("<style>", "</style>", style, (char) 0, true)); 
  
  		IPredicateRule[] result = new IPredicateRule[rules.size()];
  		rules.toArray(result);
  		setPredicateRules(result);
	  }

	};

}

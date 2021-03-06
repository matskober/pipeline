package org.daisy.dotify.impl.hyphenator.latex;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;

public class LatexHyphenatorFactory implements HyphenatorFactory {
	private final LatexHyphenatorCore core;

	/**
	 * Constructs a new LatexHypenator to be used by a hyphenator factory.
	 */
	public LatexHyphenatorFactory(LatexHyphenatorCore core) {
		this.core = core;
	}

	@Override
	public HyphenatorInterface newHyphenator(String locale) throws HyphenatorConfigurationException {
		return new LatexHyphenator(core.getHyphenator(locale));
	}

	@Override
	public Object getFeature(String key) {
		return null;
	}

	@Override
	public void setFeature(String key, Object value) throws HyphenatorConfigurationException {
		throw new LatexHyphenatorConfigurationException();
	}

}
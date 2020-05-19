package org.processmining.plugins.socialnetwork.miner;

import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetworkFactory;

/**
 * Social Network Miner Output.
 * 
 * @author Minseok Song
 * 
 */
public class SNMinerOutput {
	/**
	 * The Social network.
	 */
	private SocialNetwork sn;

	/**
	 * The weights of all elements in the Social networks
	 */
	private DirectedGraphElementWeights weights;

	/**
	 * Create default output.
	 */
	public SNMinerOutput() {
		sn = SocialNetworkFactory.newSocialNetwork("");
		weights = new DirectedGraphElementWeights();
	}

	/**
	 * Returns the social network.
	 * 
	 * @return The social network.
	 */
	public SocialNetwork getSocialNetwork() {
		return sn;
	}

	public SocialNetwork setSocialNetwork(SocialNetwork newSN) {
		SocialNetwork oldSN = sn;
		sn = newSN;
		return oldSN;
	}

	public DirectedGraphElementWeights getWeights() {
		return weights;
	}

	public DirectedGraphElementWeights setWeights(DirectedGraphElementWeights newWeights) {
		DirectedGraphElementWeights oldWeights = weights;
		weights = newWeights;
		return oldWeights;
	}
}

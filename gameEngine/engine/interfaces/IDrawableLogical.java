package engine.interfaces;

/**
 * This interface should be implemented when an object is both logical and
 * drawable. This is the case for some entity systems, and things like
 * animations etc. For further details on what these interfaces do, please check
 * the extends comments.
 * <p>
 * Note :: "Why does this class exist?" Well, it would seem odd to have an
 * interface that simply extends two interfaces, when its not actually required.
 * Sadly this is to do with java's erasures (Which is my only hatred of java)...
 * So when you try you try to do something like ::
 * 
 * <pre>
 * <code>
 * 	public void register(ILogical logicalObject){
 * 	}
 * 	
 * 	public void register(IDrawable drawable){
 * 	}
 * 	
 * 	public void <T extends ILogical & IDrawable> register(T drawableLogical){
 * 	}
 * </code>
 * </pre>
 * 
 * You will get a lovely reminder of java's failure at implementing generics
 * (backwards compatability... Not always worth it),
 * "Method register(T) has the same erasure register(ILogical) as another method in type GameLayer"
 * And due to the polymorphism ambiguity you can't use only the two register
 * methods either.
 * 
 * @author Alan Foster
 * @version 1.0
 * 
 * @see ILogical
 * @see IDrawable
 */
public interface IDrawableLogical extends ILogical, IDrawable {
}

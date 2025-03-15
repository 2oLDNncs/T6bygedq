package t6bygedq.lib;

/**
 * @author 2oLDNncs 20241108
 */
public enum ReshaperLayout {
	
	HORIZONTAL {
		
		@Override
		public final void next(final Index vIndex, final Index hIndex) {
			hIndex.next(vIndex);
		}
		
	}, VERTICAL {
		
		@Override
		public final void next(final Index vIndex, final Index hIndex) {
			vIndex.next(hIndex);
		}
		
	};
	
	public abstract void next(Index vIndex, Index hIndex);
	
}
package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.List;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0060_Library extends RecData {
	
	public final IntVar    vNumberOfMembers     = this.newIntVar(NUMBER_OF_MEMBERS);
	public final IntVar    vConcatenationNumber = this.newIntVar(CONCATENATION_NUMBER);
	
	public final StringVar vLibraryName         = this.newStringVarV(LIBRARY_NAME_LENGTH);
	public final StringVar vLibraryVolume       = this.newStringVarV(LIBRARY_VOLUME_LENGTH);
	public final StringVar vLibraryDdname       = this.newStringVarV(LIBRARY_DDNAME_LENGTH);
	
	private final List<Member> memberList = new ArrayList<>();
	
	public final Member newMember() {
		final Member result = this.new Member();
		
		memberList.add(result);
		
		return result;
	}
	
	public final Member addMember() {
		final Member result = this.newMember();
		
		this.vNumberOfMembers.set(this.vNumberOfMembers.get() + 1);
		
		return result;
	}
	
	public RecData_X0060_Library(final Buffer buffer) {
		super(buffer);
	}
	
	@Override
	protected final void afterRead() {
		super.afterRead();
		
		final int n = this.vNumberOfMembers.get();
		
		for (int i = 0; i < n; i += 1) {
			final Member m = this.newMember();
			
			Helpers.dprintlnf(" Member(%s)", i);
			Helpers.dprintlnf("  FileId<%s>", m.vFileId.get());
			Helpers.dprintlnf("  Name<%s>", m.vName.get());
		}
	}
	
	@Override
	protected final void beforeWrite() {
		if (this.vNumberOfMembers.get() != this.memberList.size()) {
			throw new IllegalStateException();
		}
	}
	
	/**
	 * @author 2oLDNncs 20250212
	 */
	public final class Member {
		
		public final IntVar vFileId = newIntVar(2);
		private final Buffer.Region nameLength = newDynamicFixedLengthRegion(2);
		public final StringVar vName = newStringVarV(this.nameLength);
		
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0060_Library.class);
	
	private static final Buffer.Region NUMBER_OF_MEMBERS     = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LIBRARY_NAME_LENGTH   = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LIBRARY_VOLUME_LENGTH = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region CONCATENATION_NUMBER  = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LIBRARY_DDNAME_LENGTH = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RESERVED_01           = staticRegionGenerator.newFixedLength(4);
	
	static {
		Helpers.ignore(RESERVED_01);
	}
	
}
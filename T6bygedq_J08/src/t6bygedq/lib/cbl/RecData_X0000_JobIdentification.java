package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.List;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250202
 */
public final class RecData_X0000_JobIdentification extends RecData {
	
	public final StringVar vDate = this.newStringVarF(DATE);
	public final StringVar vTime = this.newStringVarF(TIME);
	public final StringVar vProductNumber = this.newStringVarF(PRODUCT_NUMBER);
	public final StringVar vProductVersion = this.newStringVarF(PRODUCT_VERSION);
	public final StringVar vBuildLevel = this.newStringVarF(BUILD_LEVEL);
	public final StringVar vSystemId = this.newStringVarF(SYSTEM_ID);
	public final StringVar vJobName = this.newStringVarF(JOB_NAME);
	public final StringVar vStepName = this.newStringVarF(STEP_NAME);
	public final StringVar vProcStep = this.newStringVarF(PROC_STEP);
	public final IntVar vInputFileCount = this.newIntVar(INPUT_FILE_COUNT);
	
	private final List<InputFile> inputFileList = new ArrayList<>();
	
	public RecData_X0000_JobIdentification(final Buffer buffer) {
		super(buffer);
	}
	
	private final InputFile newInputFile() {
		final InputFile result = this.new InputFile();
		
		this.inputFileList.add(result);
		
		return result;
	}
	
	public final InputFile addInputFile() {
		final InputFile result = this.newInputFile();
		final int n = this.inputFileList.size();
		
		this.vInputFileCount.set(n);
		result.vNumber.set(n);
		
		return result;
	}
	
	@Override
	protected final void afterRead() {
		super.afterRead();
		
		final int inputFileCount = this.vInputFileCount.get();
		
		if (0 < inputFileCount) {
			for (int i = 0; i < inputFileCount; i += 1) {
				final InputFile fd = this.newInputFile();
				
				Helpers.dprintlnf(" InputFiles(%s)", i);
				Helpers.dprintlnf("  Number<%s>", fd.vNumber);
				Helpers.dprintlnf("  Name<%s>", fd.vName);
				Helpers.dprintlnf("  VolumeSerialNumber<%s>", fd.vVolumeSerialNumber);
				Helpers.dprintlnf("  MemberName<%s>", fd.vMemberName);
			}
		}
	}
	
	@Override
	protected final void beforeWrite() {
		if (this.vInputFileCount.get() != this.inputFileList.size()) {
			throw new IllegalStateException();
		}
	}
	
	public final InputFile getInputFile(final int index) {
		return this.inputFileList.get(index);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0000_JobIdentification.class);
	
	private static final Buffer.Region DATE             = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region TIME             = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PRODUCT_NUMBER   = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region PRODUCT_VERSION  = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region BUILD_LEVEL      = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region SYSTEM_ID        = staticRegionGenerator.newFixedLength(24);
	private static final Buffer.Region JOB_NAME         = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region STEP_NAME        = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region PROC_STEP        = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region INPUT_FILE_COUNT = staticRegionGenerator.newFixedLength(2);
	
	/**
	 * @author 2oLDNncs 20250204
	 */
	public final class InputFile {
		
		public final IntVar vNumber = newIntVar(2);
		private final Buffer.Region rNameLength = newDynamicFixedLengthRegion(2);
		private final Buffer.Region rVolSerialNumLength = newDynamicFixedLengthRegion(2);
		private final Buffer.Region rMemberNameLength = newDynamicFixedLengthRegion(2);
		public final StringVar vName = newStringVarV(this.rNameLength);
		public final StringVar vVolumeSerialNumber = newStringVarV(this.rVolSerialNumLength);
		public final StringVar vMemberName = newStringVarV(this.rMemberNameLength);
		
	}
	
}
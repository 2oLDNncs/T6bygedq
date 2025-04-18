package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import t6bygedq.lib.Helpers;
import t6bygedq.lib.Helpers.Debug;

/**
 * @author 2oLDNncs 20250406
 */
@Debug(false)
public abstract class JclJobParser extends JclStmtParser {
	
	private Job currentJob = null;
	
	private String inStreamProc = null;
	
	@Override
	protected void parseEnd() {
		super.parseEnd();
		this.setCurrentJob(null);
	}
	
	private final List<Step> getCurrentSteps() {
		if (null == this.inStreamProc) {
			return this.currentJob.getSteps();
		}
		
		return getOrCreate(this.currentJob.getProcs(), this.inStreamProc);
	}
	
	@Override
	protected void parseStmt(final Stmt stmt) {
		super.parseStmt(stmt);
		
		switch (stmt.getType()) {
		case K_JOB:
			this.setCurrentJob(new Job(stmt));
			break;
		case K_EXEC:
			this.getCurrentSteps().add(new Step(stmt));
			break;
		case K_DD:
			getOrCreate(Helpers.last(this.getCurrentSteps()).getDds(), stmt.getName()).add(stmt);
			break;
		case K_PROC:
			this.inStreamProc = stmt.getName();
			break;
		case K_PEND:
			this.inStreamProc = null;
			break;
		default:
			System.err.println(Helpers.dformat(stmt.errorMessage("Unhandled stmt type <%s>", stmt.getType())));
			break;
		}
	}
	
	protected void parseJob(final Job job) {
		Helpers.dprintlnf(">>>%s", this.currentJob.getStmt());
		
		this.beginJob(job);
		
		this.currentJob.getSteps().forEach(step -> {
			Helpers.dprintlnf(">>> %s", step.getStmt());
			
			this.beginStep(job, step);
			
			step.getDds().forEach((k, v) -> {
				this.beginDdGroup(job, step, k);
				
				v.forEach(stmt -> {
					Helpers.dprintlnf(">>>  %s%s", k, stmt.getParms());
					
					this.dd(job, step, k, stmt);
				});
				
				this.endDdGroup(job, step, k);
			});
			
			this.endStep(job, step);
		});
		
		this.endJob(job);
	}
	
	protected void beginJob(final Job job) {
		//pass
	}
	
	protected void beginStep(final Job job, final Step step) {
		//pass
	}
	
	protected void beginDdGroup(final Job job, final Step step, final String ddName) {
		//pass
	}
	
	protected void dd(final Job job, final Step step, final String ddName, final Stmt ddStmt) {
		final var dsn = ddStmt.findParmVal("DSN");
		
		if (null != dsn) {
			this.dsn(
					job.getStmt().getName(),
					step.getStmt().getName(),
					ddStmt.getName(),
					((Parm_Id) dsn).getVal());
		}
		
		ddStmt.getInput().forEach(line -> {
			Helpers.dprintlnf(">>>   %s", line);
		});
	}
	
	protected void endDdGroup(final Job job, final Step step, final String ddName) {
		//pass
	}
	
	protected void endStep(final Job job, final Step step) {
		//pass
	}
	
	protected void endJob(final Job job) {
		//pass
	}
	
	protected void dsn(final String jobName, final String stepName, final String ddName, final String dsn) {
		System.out.println(Helpers.dformat("%s", String.join("\t", jobName, stepName, ddName, dsn)));
	}
	
	private final void setCurrentJob(final Job currentJob) {
		if (null != this.currentJob) {
			this.parseJob(this.currentJob);
		}
		
		this.currentJob = currentJob;
	}
	
	public static final <K, V> List<V> getOrCreate(final Map<K, List<V>> map, final K key) {
		return map.computeIfAbsent(key, __ -> new ArrayList<>());
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static abstract class JclElement {
		
		private final Stmt stmt;

		protected JclElement(final Stmt stmt) {
			this.stmt = stmt;
		}
		
		public final Stmt getStmt() {
			return this.stmt;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Job extends JclElement {
		
		private final List<Step> steps = new ArrayList<>();
		
		private final Map<String, List<Step>> procs = new LinkedHashMap<>();
		
		public Job(final Stmt stmt) {
			super(stmt);
		}
		
		public final List<Step> getSteps() {
			return this.steps;
		}
		
		public final Map<String, List<Step>> getProcs() {
			return this.procs;
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250406
	 */
	public static final class Step extends JclElement {
		
		private final Map<String, List<Stmt>> dds = new LinkedHashMap<>();
		
		public Step(final Stmt stmt) {
			super(stmt);
		}
		
		public final Map<String, List<Stmt>> getDds() {
			return this.dds;
		}
		
	}
	
}
package t6bygedq.lib.cbl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250406
 */
public abstract class JclJobParser extends JclStmtParser {
	
	private Job currentJob = null;
	
	private String inStreamProc = null;
	
	@Override
	protected void parseEnd() {
		super.parseEnd();
		this.setCurrentJob(null);
	}
	
	@Override
	protected void parseStmt(final Stmt stmt) {
		super.parseStmt(stmt);
		
		switch (stmt.getType()) {
		case K_JOB:
			this.setCurrentJob(new Job(stmt));
			break;
		case K_EXEC:
			if (null == this.inStreamProc) {
				this.currentJob.getSteps().add(new Step(stmt));
			} else {
				this.currentJob.getProcs().computeIfAbsent(this.inStreamProc, __ -> new ArrayList<>()).add(new Step(stmt));
			}
			break;
		case K_DD:
			if (null == this.inStreamProc) {
				Helpers.last(this.currentJob.getSteps()).getDds().computeIfAbsent(stmt.getName(), __ -> new ArrayList<>()).add(stmt);
			} else {
				Helpers.last(this.currentJob.getProcs().get(this.inStreamProc)).getDds().computeIfAbsent(stmt.getName(), __ -> new ArrayList<>()).add(stmt);
			}
			break;
		case K_PROC:
			this.inStreamProc = stmt.getName();
			break;
		case K_PEND:
			this.inStreamProc = null;
			break;
		default:
			System.err.println(Helpers.dformat("Unexpected stmt type <%s>", stmt.getType()));
			break;
		}
	}
	
	protected void parseJob(final Job job) {
		Helpers.dprintlnf(">>>%s", this.currentJob.getStmt());
		
		this.currentJob.getSteps().forEach(step -> {
			Helpers.dprintlnf(">>> %s", step.getStmt());
			
			step.getDds().forEach((k, v) -> {
				v.forEach(stmt -> {
					Helpers.dprintlnf(">>>  %s%s", k, stmt.getParms());
					
					if (!stmt.getParms().isEmpty()) {
						final var dsnDfn = Helpers.castOrNull(ParmsToken_Dfn.class, stmt.getParms().get(0));
						
						if (null != dsnDfn && "DSN".equalsIgnoreCase(dsnDfn.getKey())) {
							this.dsn(job.getStmt().getName(), step.getStmt().getName(), stmt.getName(), dsnDfn.getValue().toString());
						}
					}
					
					stmt.getInput().forEach(line -> {
						Helpers.dprintlnf(">>>   %s", line);
					});
				});
			});
		});
	}
	
	protected void dsn(final String jobName, final String stepName, final String ddName, final String dsn) {
		System.out.println(Helpers.dformat("DSN:%s", String.join("\t", jobName, stepName, ddName, dsn)));
	}
	
	private final void setCurrentJob(final Job currentJob) {
		if (null != this.currentJob) {
			this.parseJob(this.currentJob);
		}
		
		this.currentJob = currentJob;
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
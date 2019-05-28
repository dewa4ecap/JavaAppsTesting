package com.fourelementscapital.scheduler.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import com.fourelementscapital.restapi.LaunchpadService;

@Path("/")
public class ApiController {
	
	// Run Rscript
	@POST
	@Path("action/launchpad/run/script")
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchpadRun() throws Exception {
		LaunchpadService sl = new LaunchpadService();
		return sl.runScript(
				Params.getScriptKey(),
				Params.getAlsidKey(),
				Params.getAluserKey());
	}
	
	// Show Rscript status that that being executed
	@POST
	@Path("/action/launchpad/show/status/taskid/{taskid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchpadStatus(@PathParam("taskid") String id) throws Exception {
		LaunchpadService sl = new LaunchpadService();
		return sl.getStatus(id,Params.getAlsidKey(),Params.getAluserKey());
	}
	
	// Cancel Rscript execution
	@POST
	@Path("/action/launchpad/cancel/script/taskid/{taskid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchpadCancel(@PathParam("taskid") String id) throws Exception {
		LaunchpadService sl = new LaunchpadService();
		return sl.cancelScript(
				id,
				Params.getAlsidKey(),
				Params.getAluserKey());
	}
	
	// Show console result
	@POST
	@Path("/action/launchpad/show/console/taskid/{taskid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchpadConsoleMessage(@PathParam("taskid") String id) throws Exception {
		LaunchpadService sl = new LaunchpadService();
		return sl.getConsoleMessage(
				id,
				Params.getAlsidKey(),
				Params.getAluserKey());
	}
	
	// Show backtest result
	@POST
	@Path("/action/launchpad/show/backtest/taskid/{taskid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response launchpadBacktest(@PathParam("taskid") String id) throws Exception {
		LaunchpadService sl = new LaunchpadService();
		return sl.getBacktestResult(
				id,
				Params.getAlsidKey(),
				Params.getAluserKey());
	}
	
}
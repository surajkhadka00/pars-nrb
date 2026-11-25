package np.org.nrb.pars.ai.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiQueryRouterTest {

    @Test
    void routesSelfQuestions() {
        assertEquals(AiQueryRouter.Route.SELF, AiQueryRouter.route("what is my username?"));
        assertEquals(AiQueryRouter.Route.SELF, AiQueryRouter.route("show my details"));
        assertEquals(AiQueryRouter.Route.SELF, AiQueryRouter.route("who am i"));
    }

    @Test
    void routesDealQuestions() {
        assertEquals(AiQueryRouter.Route.DEALS, AiQueryRouter.route("show my deals"));
        assertEquals(AiQueryRouter.Route.DEALS, AiQueryRouter.route("list deals please"));
    }

    @Test
    void routesHowToAddDealsToAppGuide() {
        assertEquals(AiQueryRouter.Route.APP_GUIDE, AiQueryRouter.route("how to add the deals"));
        assertEquals(AiQueryRouter.Route.APP_GUIDE, AiQueryRouter.route("how do i create a deal?"));
    }

    @Test
    void routesCapabilityAndDocumentQuestions() {
        assertEquals(AiQueryRouter.Route.CAPABILITY, AiQueryRouter.route("how can you help me ?"));
        assertEquals(AiQueryRouter.Route.DOCUMENTS,
                AiQueryRouter.route("Who approves FX deals above NPR 1,000,000?"));
    }

    @Test
    void routesForexAndNrbDocumentQuestions() {
        assertEquals(AiQueryRouter.Route.FOREX_LIVE, AiQueryRouter.route("What is today's USD exchange rate?"));
        assertEquals(AiQueryRouter.Route.FOREX_HISTORICAL, AiQueryRouter.route("USD exchange rate on 2026-01-01?"));
        assertEquals(AiQueryRouter.Route.NRB_DOCUMENT, AiQueryRouter.route("Find the latest foreign exchange circular"));
        assertEquals(AiQueryRouter.Route.NRB_DOCUMENT, AiQueryRouter.route("Explain the latest monetary policy"));
    }
}

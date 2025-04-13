import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;
import ru.tinkoff.piapi.core.models.FuturePosition;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.SecurityPosition;
import ru.tinkoff.piapi.core.stream.StreamProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
// import java.util.Currency;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.stream.Collectors;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

public class Main {

    static class Item {
        String name;
        BigDecimal value;

        public Item(String ticker, BigDecimal data) {
            name = ticker;
            value = data;
        }
    }

    static int iFigi = 0;
    static figi mFigi = new figi();

    static List<Item> mList = new ArrayList<Item>();

    static final Logger log = LoggerFactory.getLogger(Main.class);
    // private static final Logger log = Logger.getLogger(Main.class.getName());

    static String mToken;

    static InvestApi mApi = InvestApi.create(mToken);
    static List<Account> mListAccount = mApi.getUserService().getAccountsSync();
    static Account mAccount = mListAccount.get(0);

    static int count = 0;

    static class CProcess extends TimerTask {
        public void run() {

            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

                count++;
                var portfolio = mApi.getOperationsService().getPortfolioSync(mAccount.getId());
                var positions = portfolio.getPositions();

                int size = positions.size();
                for(int i = 0; i < size; i++)
                    mList.get(i).value = positions.get(i).getCurrentPrice().getValue();

                show();

                /*for (Position position : positions) {

                    var instrument = mApi.getInstrumentsService().getInstrumentByFigiSync(position.getFigi());
                    count++;
                    System.out.println(instrument.getTicker() + ":\t" + );
                }*/

            } catch (InterruptedException | IOException | ApiRuntimeException e) {
                e.printStackTrace();
                System.out.println(count);
                System.exit(0);
            }
        }
    }

    static void show() {

        System.out.println(count);
        for(Item item: mList)
            System.out.println(item.name + ":\t" + item.value);

    }

    static Timer getListFigi = new Timer();

    public static void main(String[] args) {

        /*if(args.length == 2) {
            InputStream is = Main.class.getClassLoader().getResourceAsStream(args[1]);
            if(is != null ) {

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                System.out.println();
            }
        }*/

        // getListFigi.schedule(new getListFigi(), 0, 300);

        /*var portfolio = mApi.getOperationsService().getPortfolioSync(mAccount.getId());
        var positions = portfolio.getPositions();

        for (Position position : positions) {

            var instrument = mApi.getInstrumentsService().getInstrumentByFigiSync(position.getFigi());

            mList.add(new Item(instrument.getTicker(), position.getCurrentPrice().getValue()));
            // System.out.println(instrument.getTicker() + ":\t" + position.getCurrentPrice().getValue());
        }*/

        //show();

        /* main process */ // (new Timer()).schedule(new CProcess(), 0, 1000);

        /*try {
            var accounts = mApi.getUserService().getAccountsSync();
            var mainAccount = accounts.get(0);
            for (Account account : accounts) {
                log.info("account id: {}, access level: {}", account.getId(), account.getAccessLevel().name());
                // System.out.println("account id: " + account.getId() + " access level: " + account.getAccessLevel().name());
            }


            var positions2 = mApi.getOperationsService().getPositionsSync(mainAccount.getId());

            log.info("список ценно-бумажных позиций портфеля");
            var securities = positions2.getSecurities();
            for (SecurityPosition security : securities) {
                var figi = security.getFigi();
                var balance = security.getBalance();
                var blocked = security.getBlocked();
                log.info("figi: {}, текущий баланс: {}, заблокировано: {}", figi, balance, blocked);
            }

            var portfolio = mApi.getOperationsService().getPortfolioSync(mainAccount.getId());
            var positions = portfolio.getPositions();
            log.info("в портфолио {} позиций", positions.size());
            for (int i = 0; i < Math.max(positions.size(), 5); i++) {
                var position = positions.get(i);
                var figi = position.getFigi();
                var quantity = position.getQuantity();
                var currentPrice = position.getCurrentPrice();
                var expectedYield = position.getExpectedYield();
                log.info(
                        "позиция с figi: {}, количество инструмента: {}, текущая цена инструмента: {}, текущая расчитанная " +
                                "доходность: {}",
                        figi, quantity, currentPrice.getValue(), expectedYield);


                var instrument = mApi.getInstrumentsService().getInstrumentByFigiSync(figi);
                log.info(
                        "инструмент figi: {}, лотность: {}, текущий режим торгов: {}, признак внебиржи: {}, признак доступности торгов " +
                                "через api : {}",
                        instrument.getFigi(),
                        instrument.getLot(),
                        instrument.getTradingStatus().name(),
                        instrument.getOtcFlag(),
                        instrument.getApiTradeAvailableFlag());
            }

        } catch (ApiRuntimeException e) {
            e.printStackTrace();
        }

        var list = mApi.getInstrumentsService().getAllCurrenciesSync();
        for (Currency cur : list) {

            var a1 = cur.getFigi();
            var a2 = cur.getTicker();
            var a3 = cur.getClassCode();
            var a4 = cur.getIsin();
            var a5 = cur.getLot();
            var a6 = cur.getCurrency();
            var a7 = cur.getKlong();
            var a8 = cur.getKshort();
            var a9 = cur.getDlong();
            var a10 = cur.getDshort();
            var a11 = cur.getDlongMin();
            var a12 = cur.getDshortMin();
            var a13 = cur.getShortEnabledFlag();
            var a14 = cur.getName();
            var a15 = cur.getExchange();
            var a16 = cur.getNominal();
            var a17 = cur.getCountryOfRisk();
            var a18 = cur.getCountryOfRiskName();
            var a19 = cur.getTradingStatus();
            var a20 = cur.getOtcFlag();
            var a21 = cur.getBuyAvailableFlag();
            var a22 = cur.getSellAvailableFlag();
            var a23 = cur.getIsoCurrencyName();
            var a24 = cur.getMinPriceIncrement();
            var a25 = cur.getApiTradeAvailableFlag();
            var a26 = cur.getUid();
            var a27 = cur.getRealExchange();

            var lastPrices = mApi.getMarketDataService().getLastPricesSync(List.of(a1));
            for (LastPrice lastPrice : lastPrices) {
                var figi = lastPrice.getFigi();
                var price = quotationToBigDecimal(lastPrice.getPrice());
                var time = timestampToString(lastPrice.getTime());
                log.info("последняя цена по инструменту {}, цена: {}, время обновления цены: {}", figi, price, time);
            }

            log.info("актив. uid : {}.{}, имя: {}, тип: {}", a1, a2, cur.getName(), cur.getCurrency());
        }

        // getWithdrawLimitsExample(api);

        // getPortfolioExample(api);

        // getPositionsExample(api);

        var assets = mApi.getInstrumentsService().getAssetsSync().stream().limit(21).collect(Collectors.toList());
        for (Asset asset : assets) {
            log.info("актив. uid : {}, имя: {}, тип: {}", asset.getUid(), asset.getName(), asset.getType());
            var assetBy = mApi.getInstrumentsService().getAssetBySync(asset.getUid());
            log.info("подробная информация об активе. описание: {}, статус: {}, бренд: {}", assetBy.getDescription(), assetBy.getStatus(), assetBy.getBrand().getInfo());
        }

        var uid = assets.get(0).getUid();
        var assetBy = mApi.getInstrumentsService().getAssetBySync(uid);
        log.info("подробная информация об активе. описание: {}, статус: {}, бренд: {}", assetBy.getDescription(), assetBy.getStatus(), assetBy.getBrand().getInfo());


        // var randomFigi = randomFigi(api, 5);

        List<String> randomFigi = List.of("BBG000BBJQV0");

        var lastPrices = mApi.getMarketDataService().getLastPricesSync(randomFigi);
        for (LastPrice lastPrice : lastPrices) {
            var figi = lastPrice.getFigi();
            var price = quotationToBigDecimal(lastPrice.getPrice());
            var time = timestampToString(lastPrice.getTime());
            log.info("последняя цена по инструменту {}, цена: {}, время обновления цены: {}", figi, price, time);
        }*/
    }

    static class getListFigi extends TimerTask {
        public void run() {

            Instrument instrument;

            String figi = mFigi.list.get(iFigi++);
            String name = "Инструмент не найден";

            try {

                instrument = mApi.getInstrumentsService().getInstrumentByFigiSync(figi);
                name = instrument.getName();
                figi = instrument.getFigi();

            } catch(Exception e) {
                int code = e.hashCode();
                if(code == 80002)
                    getListFigi.cancel();
                e.printStackTrace();
            }

            System.out.println(String.valueOf(iFigi) + '\t' + figi + '\t' + name);

            /*log.info(
                    "инструмент figi: {}, лотность: {}, текущий режим торгов: {}, признак внебиржи: {}, признак доступности торгов " +
                            "через api : {}",
                    instrument.getFigi(),
                    instrument.getLot(),
                    instrument.getTradingStatus().name(),
                    instrument.getOtcFlag(),
                    instrument.getApiTradeAvailableFlag());*/
        }
    }


    private static List<String> randomFigi(InvestApi api, int count) {
        return api.getInstrumentsService().getTradableSharesSync()
                .stream()
                .filter(el -> Boolean.TRUE.equals(el.getApiTradeAvailableFlag()))
                .map(Share::getFigi)
                .limit(count)
                .collect(Collectors.toList());
    }

    private static void portfolioStreamExample(InvestApi api, Account account) {
        StreamProcessor<PortfolioStreamResponse> consumer = response -> {

            var ping = response.hasPing();
            if (ping) {
                // log.info("пинг сообщение");
                System.out.println("пинг сообщение");
            } else if (response.hasPortfolio()) {
                // log.info("Новые данные по портфолио: {}", response);
                System.out.println("Новые данные по портфолио: " + response);
            }
        };

        Consumer<Throwable> onErrorCallback = error -> {
            // log.error(error.toString());
            System.out.println(error.toString());
        };

        var accountId1 = "my_account_id1";
        var accountId2 = "my_account_id2";
        //Подписка стрим портфолио. Не блокирующий вызов
        //При необходимости обработки ошибок (реконнект по вине сервера или клиента), рекомендуется сделать onErrorCallback
        // api.getOperationsStreamService().subscribePortfolio(consumer, onErrorCallback, accountId1);
        api.getOperationsStreamService().subscribePortfolio(consumer, onErrorCallback, account.getId());

        //Если обработка ошибок не требуется, то можно использовать перегруженный метод
        // api.getOperationsStreamService().subscribePortfolio(consumer, accountId2);

        //Если требуется подписаться на обновление сразу по нескольким accountId - можно передать список
        // api.getOperationsStreamService().subscribePortfolio(consumer, List.of(accountId1, accountId2));
    }


    private static void usersServiceExample(InvestApi api) {
        //Получаем список аккаунтов и распечатываем их с указанием привилегий токена
        var accounts = api.getUserService().getAccountsSync();
        var mainAccount = accounts.get(0);
        for (Account account : accounts) {
            log.info("account id: {}, access level: {}", account.getId(), account.getAccessLevel().name());
        }

        //Получаем и печатаем информацию о текущих лимитах пользователя
        var tariff = api.getUserService().getUserTariffSync();
        log.info("stream type: marketdata, stream limit: {}", tariff.getStreamLimitsList().get(0).getLimit());
        log.info("stream type: orders, stream limit: {}", tariff.getStreamLimitsList().get(1).getLimit());
        log.info("current unary limit per minute: {}", tariff.getUnaryLimitsList().get(0).getLimitPerMinute());

        try {

            //Получаем и печатаем информацию об обеспеченности портфеля
            var marginAttributes = api.getUserService().getMarginAttributesSync(mainAccount.getId());
            log.info("Ликвидная стоимость портфеля: {}", moneyValueToBigDecimal(marginAttributes.getLiquidPortfolio()));
            log.info("Начальная маржа — начальное обеспечение для совершения новой сделки: {}",
                    moneyValueToBigDecimal(marginAttributes.getStartingMargin()));
            log.info("Минимальная маржа — это минимальное обеспечение для поддержания позиции, которую вы уже открыли: {}",
                    moneyValueToBigDecimal(marginAttributes.getMinimalMargin()));
            log.info("Уровень достаточности средств. Соотношение стоимости ликвидного портфеля к начальной марже: {}",
                    quotationToBigDecimal(marginAttributes.getFundsSufficiencyLevel()));
            log.info("Объем недостающих средств. Разница между стартовой маржой и ликвидной стоимости портфеля: {}",
                    moneyValueToBigDecimal(marginAttributes.getAmountOfMissingFunds()));


        } catch (ApiRuntimeException e) {
            e.printStackTrace();
        }
    }

    private static void getWithdrawLimitsExample(InvestApi api) {
        var accounts = api.getUserService().getAccountsSync();
        var mainAccount = accounts.get(0).getId();

        var withdrawLimits = api.getOperationsService().getWithdrawLimitsSync(mainAccount);
        var money = withdrawLimits.getMoney();
        var blocked = withdrawLimits.getBlocked();
        var blockedGuarantee = withdrawLimits.getBlockedGuarantee();

        log.info("доступный для вывода остаток для счета {}", mainAccount);
        log.info("массив валютных позиций");
        for (Money moneyValue : money) {
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getValue());
        }

        log.info("массив заблокированных валютных позиций портфеля");
        for (Money moneyValue : blocked) {
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getValue());
        }

        log.info("заблокировано под гарантийное обеспечение фьючерсов");
        for (Money moneyValue : blockedGuarantee) {
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getValue());
        }
    }

    private static void getPortfolioExample(InvestApi api) {
        var accounts = api.getUserService().getAccountsSync();
        var mainAccount = accounts.get(0).getId();

        //Получаем и печатаем портфолио
        var portfolio = api.getOperationsService().getPortfolioSync(mainAccount);
        var totalAmountBonds = portfolio.getTotalAmountBonds();
        log.info("общая стоимость облигаций в портфеле {}", totalAmountBonds);

        var totalAmountEtf = portfolio.getTotalAmountEtfs();
        log.info("общая стоимость фондов в портфеле {}", totalAmountEtf);

        var totalAmountCurrencies = portfolio.getTotalAmountCurrencies();
        log.info("общая стоимость валют в портфеле {}", totalAmountCurrencies);

        var totalAmountFutures = portfolio.getTotalAmountFutures();
        log.info("общая стоимость фьючерсов в портфеле {}", totalAmountFutures);

        var totalAmountShares = portfolio.getTotalAmountShares();
        log.info("общая стоимость акций в портфеле {}", totalAmountShares);

        log.info("текущая доходность портфеля {}", portfolio.getExpectedYield());

        var positions = portfolio.getPositions();
        log.info("в портфолио {} позиций", positions.size());
        for (int i = 0; i < Math.min(positions.size(), 5); i++) {
            var position = positions.get(i);
            var figi = position.getFigi();
            var quantity = position.getQuantity();
            var currentPrice = position.getCurrentPrice();
            var expectedYield = position.getExpectedYield();
            log.info("позиция с figi: {}, количество инструмента: {}, текущая цена инструмента: {}, текущая расчитанная " + "доходность: {}", figi, quantity, currentPrice, expectedYield);
        }
    }

    private static void getPositionsExample(InvestApi api) {
        var accounts = api.getUserService().getAccountsSync();
        var mainAccount = accounts.get(0).getId();
        //Получаем и печатаем список позиций
        var positions = api.getOperationsService().getPositionsSync(mainAccount);

        log.info("список валютных позиций портфеля");
        var moneyList = positions.getMoney();
        for (Money moneyValue : moneyList) {
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getValue());
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getCurrency());
        }

        log.info("список заблокированных валютных позиций портфеля");
        var blockedList = positions.getBlocked();
        for (Money moneyValue : blockedList) {
            log.info("валюта: {}, количество: {}", moneyValue.getCurrency(), moneyValue.getValue());
        }

        log.info("список ценно-бумажных позиций портфеля");
        var securities = positions.getSecurities();
        for (SecurityPosition security : securities) {
            var figi = security.getFigi();
            var balance = security.getBalance();
            var blocked = security.getBlocked();
            log.info("figi: {}, текущий баланс: {}, заблокировано: {}", figi, balance, blocked);
        }

        log.info("список фьючерсов портфеля");
        var futuresList = positions.getFutures();
        for (FuturePosition security : futuresList) {
            var figi = security.getFigi();
            var balance = security.getBalance();
            var blocked = security.getBlocked();
            log.info("figi: {}, текущий баланс: {}, заблокировано: {}", figi, balance, blocked);
        }
    }
}

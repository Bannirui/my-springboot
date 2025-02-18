package org.elasticsearch.client;

import java.io.Closeable;

public class RestHighLevelClient implements Closeable {
    private final RestClient client;
    private final NamedXContentRegistry registry;
    private final CheckedConsumer<RestClient, IOException> doClose;
    private final IndicesClient indicesClient;
    private final ClusterClient clusterClient;
    private final IngestClient ingestClient;
    private final SnapshotClient snapshotClient;
    private final TasksClient tasksClient;
    private final XPackClient xPackClient;
    private final WatcherClient watcherClient;
    private final GraphClient graphClient;
    private final LicenseClient licenseClient;
    private final IndexLifecycleClient indexLifecycleClient;
    private final MigrationClient migrationClient;
    private final MachineLearningClient machineLearningClient;
    private final SecurityClient securityClient;
    private final RollupClient rollupClient;
    private final CcrClient ccrClient;
    private static final DeprecationHandler DEPRECATION_HANDLER = new DeprecationHandler() {
        public void usedDeprecatedName(String usedName, String modernName) {
        }

        public void usedDeprecatedField(String usedName, String replacedWith) {
        }

        public void deprecated(String message, Object... params) {
        }
    };

    public RestHighLevelClient(RestClientBuilder restClientBuilder) {
        this(restClientBuilder, Collections.emptyList());
    }

    protected RestHighLevelClient(RestClientBuilder restClientBuilder, List<Entry> namedXContentEntries) {
        this(restClientBuilder.build(), RestClient::close, namedXContentEntries);
    }

    protected RestHighLevelClient(RestClient restClient, CheckedConsumer<RestClient, IOException> doClose, List<Entry> namedXContentEntries) {
        this.indicesClient = new IndicesClient(this);
        this.clusterClient = new ClusterClient(this);
        this.ingestClient = new IngestClient(this);
        this.snapshotClient = new SnapshotClient(this);
        this.tasksClient = new TasksClient(this);
        this.xPackClient = new XPackClient(this);
        this.watcherClient = new WatcherClient(this);
        this.graphClient = new GraphClient(this);
        this.licenseClient = new LicenseClient(this);
        this.indexLifecycleClient = new IndexLifecycleClient(this);
        this.migrationClient = new MigrationClient(this);
        this.machineLearningClient = new MachineLearningClient(this);
        this.securityClient = new SecurityClient(this);
        this.rollupClient = new RollupClient(this);
        this.ccrClient = new CcrClient(this);
        this.client = (RestClient)Objects.requireNonNull(restClient, "restClient must not be null");
        this.doClose = (CheckedConsumer)Objects.requireNonNull(doClose, "doClose consumer must not be null");
        this.registry = new NamedXContentRegistry((List)Stream.of(getDefaultNamedXContents().stream(), getProvidedNamedXContents().stream(), namedXContentEntries.stream()).flatMap(Function.identity()).collect(Collectors.toList()));
    }

    public final RestClient getLowLevelClient() {
        return this.client;
    }

    public final void close() throws IOException {
        this.doClose.accept(this.client);
    }

    public final IndicesClient indices() {
        return this.indicesClient;
    }

    public final ClusterClient cluster() {
        return this.clusterClient;
    }

    public final IngestClient ingest() {
        return this.ingestClient;
    }

    public final SnapshotClient snapshot() {
        return this.snapshotClient;
    }

    public RollupClient rollup() {
        return this.rollupClient;
    }

    public final CcrClient ccr() {
        return this.ccrClient;
    }

    public final TasksClient tasks() {
        return this.tasksClient;
    }

    public final XPackClient xpack() {
        return this.xPackClient;
    }

    public WatcherClient watcher() {
        return this.watcherClient;
    }

    public GraphClient graph() {
        return this.graphClient;
    }

    public LicenseClient license() {
        return this.licenseClient;
    }

    public IndexLifecycleClient indexLifecycle() {
        return this.indexLifecycleClient;
    }

    public MigrationClient migration() {
        return this.migrationClient;
    }

    public MachineLearningClient machineLearning() {
        return this.machineLearningClient;
    }

    public SecurityClient security() {
        return this.securityClient;
    }

    public final BulkResponse bulk(BulkRequest bulkRequest, RequestOptions options) throws IOException {
        return (BulkResponse)this.performRequestAndParseEntity((ActionRequest)bulkRequest, RequestConverters::bulk, (RequestOptions)options, (CheckedFunction)(BulkResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final BulkResponse bulk(BulkRequest bulkRequest, Header... headers) throws IOException {
        return (BulkResponse)this.performRequestAndParseEntity((ActionRequest)bulkRequest, RequestConverters::bulk, (CheckedFunction)(BulkResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void bulkAsync(BulkRequest bulkRequest, RequestOptions options, ActionListener<BulkResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)bulkRequest, RequestConverters::bulk, (RequestOptions)options, (CheckedFunction)(BulkResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void bulkAsync(BulkRequest bulkRequest, ActionListener<BulkResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)bulkRequest, RequestConverters::bulk, (CheckedFunction)(BulkResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final BulkByScrollResponse reindex(ReindexRequest reindexRequest, RequestOptions options) throws IOException {
        return (BulkByScrollResponse)this.performRequestAndParseEntity((ActionRequest)reindexRequest, RequestConverters::reindex, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final TaskSubmissionResponse submitReindexTask(ReindexRequest reindexRequest, RequestOptions options) throws IOException {
        return (TaskSubmissionResponse)this.performRequestAndParseEntity((ActionRequest)reindexRequest, RequestConverters::submitReindex, (RequestOptions)options, (CheckedFunction)(TaskSubmissionResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void reindexAsync(ReindexRequest reindexRequest, RequestOptions options, ActionListener<BulkByScrollResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)reindexRequest, RequestConverters::reindex, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final BulkByScrollResponse updateByQuery(UpdateByQueryRequest updateByQueryRequest, RequestOptions options) throws IOException {
        return (BulkByScrollResponse)this.performRequestAndParseEntity((ActionRequest)updateByQueryRequest, RequestConverters::updateByQuery, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void updateByQueryAsync(UpdateByQueryRequest updateByQueryRequest, RequestOptions options, ActionListener<BulkByScrollResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)updateByQueryRequest, RequestConverters::updateByQuery, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final BulkByScrollResponse deleteByQuery(DeleteByQueryRequest deleteByQueryRequest, RequestOptions options) throws IOException {
        return (BulkByScrollResponse)this.performRequestAndParseEntity((ActionRequest)deleteByQueryRequest, RequestConverters::deleteByQuery, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void deleteByQueryAsync(DeleteByQueryRequest deleteByQueryRequest, RequestOptions options, ActionListener<BulkByScrollResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)deleteByQueryRequest, RequestConverters::deleteByQuery, (RequestOptions)options, (CheckedFunction)(BulkByScrollResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final ListTasksResponse deleteByQueryRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options) throws IOException {
        return (ListTasksResponse)this.performRequestAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleDeleteByQuery, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void deleteByQueryRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options, ActionListener<ListTasksResponse> listener) {
        this.performRequestAsyncAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleDeleteByQuery, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final ListTasksResponse updateByQueryRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options) throws IOException {
        return (ListTasksResponse)this.performRequestAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleUpdateByQuery, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void updateByQueryRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options, ActionListener<ListTasksResponse> listener) {
        this.performRequestAsyncAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleUpdateByQuery, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final ListTasksResponse reindexRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options) throws IOException {
        return (ListTasksResponse)this.performRequestAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleReindex, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void reindexRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options, ActionListener<ListTasksResponse> listener) {
        this.performRequestAsyncAndParseEntity((Validatable)rethrottleRequest, RequestConverters::rethrottleReindex, (RequestOptions)options, (CheckedFunction)(ListTasksResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final boolean ping(RequestOptions options) throws IOException {
        return (Boolean)this.performRequest((ActionRequest)(new MainRequest()), (request) -> {
            return RequestConverters.ping();
        }, (RequestOptions)options, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final boolean ping(Header... headers) throws IOException {
        return (Boolean)this.performRequest((ActionRequest)(new MainRequest()), (request) -> {
            return RequestConverters.ping();
        }, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final MainResponse info(RequestOptions options) throws IOException {
        return (MainResponse)this.performRequestAndParseEntity((ActionRequest)(new MainRequest()), (request) -> {
            return RequestConverters.info();
        }, (RequestOptions)options, (CheckedFunction)(MainResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final MainResponse info(Header... headers) throws IOException {
        return (MainResponse)this.performRequestAndParseEntity((ActionRequest)(new MainRequest()), (request) -> {
            return RequestConverters.info();
        }, (CheckedFunction)(MainResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final GetResponse get(GetRequest getRequest, RequestOptions options) throws IOException {
        return (GetResponse)this.performRequestAndParseEntity((ActionRequest)getRequest, RequestConverters::get, (RequestOptions)options, (CheckedFunction)(GetResponse::fromXContent), (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final GetResponse get(GetRequest getRequest, Header... headers) throws IOException {
        return (GetResponse)this.performRequestAndParseEntity((ActionRequest)getRequest, RequestConverters::get, (CheckedFunction)(GetResponse::fromXContent), (Set)Collections.singleton(404), (Header[])headers);
    }

    public final void getAsync(GetRequest getRequest, RequestOptions options, ActionListener<GetResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)getRequest, RequestConverters::get, (RequestOptions)options, (CheckedFunction)(GetResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final void getAsync(GetRequest getRequest, ActionListener<GetResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)getRequest, RequestConverters::get, (CheckedFunction)(GetResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final MultiGetResponse multiGet(MultiGetRequest multiGetRequest, RequestOptions options) throws IOException {
        return this.mget(multiGetRequest, options);
    }

    public final MultiGetResponse mget(MultiGetRequest multiGetRequest, RequestOptions options) throws IOException {
        return (MultiGetResponse)this.performRequestAndParseEntity((ActionRequest)multiGetRequest, RequestConverters::multiGet, (RequestOptions)options, (CheckedFunction)(MultiGetResponse::fromXContent), (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final MultiGetResponse multiGet(MultiGetRequest multiGetRequest, Header... headers) throws IOException {
        return (MultiGetResponse)this.performRequestAndParseEntity((ActionRequest)multiGetRequest, RequestConverters::multiGet, (CheckedFunction)(MultiGetResponse::fromXContent), (Set)Collections.singleton(404), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final void multiGetAsync(MultiGetRequest multiGetRequest, RequestOptions options, ActionListener<MultiGetResponse> listener) {
        this.mgetAsync(multiGetRequest, options, listener);
    }

    public final void mgetAsync(MultiGetRequest multiGetRequest, RequestOptions options, ActionListener<MultiGetResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)multiGetRequest, RequestConverters::multiGet, (RequestOptions)options, (CheckedFunction)(MultiGetResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final void multiGetAsync(MultiGetRequest multiGetRequest, ActionListener<MultiGetResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)multiGetRequest, RequestConverters::multiGet, (CheckedFunction)(MultiGetResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404), (Header[])headers);
    }

    public final boolean exists(GetRequest getRequest, RequestOptions options) throws IOException {
        return (Boolean)this.performRequest((ActionRequest)getRequest, RequestConverters::exists, (RequestOptions)options, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final boolean exists(GetRequest getRequest, Header... headers) throws IOException {
        return (Boolean)this.performRequest((ActionRequest)getRequest, RequestConverters::exists, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void existsAsync(GetRequest getRequest, RequestOptions options, ActionListener<Boolean> listener) {
        this.performRequestAsync((ActionRequest)getRequest, RequestConverters::exists, (RequestOptions)options, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void existsAsync(GetRequest getRequest, ActionListener<Boolean> listener, Header... headers) {
        this.performRequestAsync((ActionRequest)getRequest, RequestConverters::exists, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public boolean existsSource(GetRequest getRequest, RequestOptions options) throws IOException {
        return (Boolean)this.performRequest((ActionRequest)getRequest, RequestConverters::sourceExists, (RequestOptions)options, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (Set)Collections.emptySet());
    }

    public final void existsSourceAsync(GetRequest getRequest, RequestOptions options, ActionListener<Boolean> listener) {
        this.performRequestAsync((ActionRequest)getRequest, RequestConverters::sourceExists, (RequestOptions)options, (CheckedFunction)(RestHighLevelClient::convertExistsResponse), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final IndexResponse index(IndexRequest indexRequest, RequestOptions options) throws IOException {
        return (IndexResponse)this.performRequestAndParseEntity((ActionRequest)indexRequest, RequestConverters::index, (RequestOptions)options, (CheckedFunction)(IndexResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final IndexResponse index(IndexRequest indexRequest, Header... headers) throws IOException {
        return (IndexResponse)this.performRequestAndParseEntity((ActionRequest)indexRequest, RequestConverters::index, (CheckedFunction)(IndexResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void indexAsync(IndexRequest indexRequest, RequestOptions options, ActionListener<IndexResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)indexRequest, RequestConverters::index, (RequestOptions)options, (CheckedFunction)(IndexResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void indexAsync(IndexRequest indexRequest, ActionListener<IndexResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)indexRequest, RequestConverters::index, (CheckedFunction)(IndexResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final CountResponse count(CountRequest countRequest, RequestOptions options) throws IOException {
        return (CountResponse)this.performRequestAndParseEntity((ActionRequest)countRequest, RequestConverters::count, (RequestOptions)options, (CheckedFunction)(CountResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void countAsync(CountRequest countRequest, RequestOptions options, ActionListener<CountResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)countRequest, RequestConverters::count, (RequestOptions)options, (CheckedFunction)(CountResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final UpdateResponse update(UpdateRequest updateRequest, RequestOptions options) throws IOException {
        return (UpdateResponse)this.performRequestAndParseEntity((ActionRequest)updateRequest, RequestConverters::update, (RequestOptions)options, (CheckedFunction)(UpdateResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final UpdateResponse update(UpdateRequest updateRequest, Header... headers) throws IOException {
        return (UpdateResponse)this.performRequestAndParseEntity((ActionRequest)updateRequest, RequestConverters::update, (CheckedFunction)(UpdateResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void updateAsync(UpdateRequest updateRequest, RequestOptions options, ActionListener<UpdateResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)updateRequest, RequestConverters::update, (RequestOptions)options, (CheckedFunction)(UpdateResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void updateAsync(UpdateRequest updateRequest, ActionListener<UpdateResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)updateRequest, RequestConverters::update, (CheckedFunction)(UpdateResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final DeleteResponse delete(DeleteRequest deleteRequest, RequestOptions options) throws IOException {
        return (DeleteResponse)this.performRequestAndParseEntity((ActionRequest)deleteRequest, RequestConverters::delete, (RequestOptions)options, (CheckedFunction)(DeleteResponse::fromXContent), (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final DeleteResponse delete(DeleteRequest deleteRequest, Header... headers) throws IOException {
        return (DeleteResponse)this.performRequestAndParseEntity((ActionRequest)deleteRequest, RequestConverters::delete, (CheckedFunction)(DeleteResponse::fromXContent), (Set)Collections.singleton(404), (Header[])headers);
    }

    public final void deleteAsync(DeleteRequest deleteRequest, RequestOptions options, ActionListener<DeleteResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)deleteRequest, RequestConverters::delete, (RequestOptions)options, (CheckedFunction)(DeleteResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404));
    }

    /** @deprecated */
    @Deprecated
    public final void deleteAsync(DeleteRequest deleteRequest, ActionListener<DeleteResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)deleteRequest, RequestConverters::delete, (CheckedFunction)(DeleteResponse::fromXContent), (ActionListener)listener, (Set)Collections.singleton(404), (Header[])headers);
    }

    public final SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException {
        return (SearchResponse)this.performRequestAndParseEntity((ActionRequest)searchRequest, (r) -> {
            return RequestConverters.search(r, "_search");
        }, (RequestOptions)options, (CheckedFunction)(SearchResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final SearchResponse search(SearchRequest searchRequest, Header... headers) throws IOException {
        return (SearchResponse)this.performRequestAndParseEntity((ActionRequest)searchRequest, (r) -> {
            return RequestConverters.search(r, "_search");
        }, (CheckedFunction)(SearchResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void searchAsync(SearchRequest searchRequest, RequestOptions options, ActionListener<SearchResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchRequest, (r) -> {
            return RequestConverters.search(r, "_search");
        }, (RequestOptions)options, (CheckedFunction)(SearchResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void searchAsync(SearchRequest searchRequest, ActionListener<SearchResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchRequest, (r) -> {
            return RequestConverters.search(r, "_search");
        }, (CheckedFunction)(SearchResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final MultiSearchResponse multiSearch(MultiSearchRequest multiSearchRequest, RequestOptions options) throws IOException {
        return this.msearch(multiSearchRequest, options);
    }

    public final MultiSearchResponse msearch(MultiSearchRequest multiSearchRequest, RequestOptions options) throws IOException {
        return (MultiSearchResponse)this.performRequestAndParseEntity((ActionRequest)multiSearchRequest, RequestConverters::multiSearch, (RequestOptions)options, (CheckedFunction)(MultiSearchResponse::fromXContext), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final MultiSearchResponse multiSearch(MultiSearchRequest multiSearchRequest, Header... headers) throws IOException {
        return (MultiSearchResponse)this.performRequestAndParseEntity((ActionRequest)multiSearchRequest, RequestConverters::multiSearch, (CheckedFunction)(MultiSearchResponse::fromXContext), (Set)Collections.emptySet(), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final void multiSearchAsync(MultiSearchRequest searchRequest, RequestOptions options, ActionListener<MultiSearchResponse> listener) {
        this.msearchAsync(searchRequest, options, listener);
    }

    public final void msearchAsync(MultiSearchRequest searchRequest, RequestOptions options, ActionListener<MultiSearchResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchRequest, RequestConverters::multiSearch, (RequestOptions)options, (CheckedFunction)(MultiSearchResponse::fromXContext), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void multiSearchAsync(MultiSearchRequest searchRequest, ActionListener<MultiSearchResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchRequest, RequestConverters::multiSearch, (CheckedFunction)(MultiSearchResponse::fromXContext), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final SearchResponse searchScroll(SearchScrollRequest searchScrollRequest, RequestOptions options) throws IOException {
        return this.scroll(searchScrollRequest, options);
    }

    public final SearchResponse scroll(SearchScrollRequest searchScrollRequest, RequestOptions options) throws IOException {
        return (SearchResponse)this.performRequestAndParseEntity((ActionRequest)searchScrollRequest, RequestConverters::searchScroll, (RequestOptions)options, (CheckedFunction)(SearchResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final SearchResponse searchScroll(SearchScrollRequest searchScrollRequest, Header... headers) throws IOException {
        return (SearchResponse)this.performRequestAndParseEntity((ActionRequest)searchScrollRequest, RequestConverters::searchScroll, (CheckedFunction)(SearchResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    /** @deprecated */
    @Deprecated
    public final void searchScrollAsync(SearchScrollRequest searchScrollRequest, RequestOptions options, ActionListener<SearchResponse> listener) {
        this.scrollAsync(searchScrollRequest, options, listener);
    }

    public final void scrollAsync(SearchScrollRequest searchScrollRequest, RequestOptions options, ActionListener<SearchResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchScrollRequest, RequestConverters::searchScroll, (RequestOptions)options, (CheckedFunction)(SearchResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void searchScrollAsync(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchScrollRequest, RequestConverters::searchScroll, (CheckedFunction)(SearchResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final ClearScrollResponse clearScroll(ClearScrollRequest clearScrollRequest, RequestOptions options) throws IOException {
        return (ClearScrollResponse)this.performRequestAndParseEntity((ActionRequest)clearScrollRequest, RequestConverters::clearScroll, (RequestOptions)options, (CheckedFunction)(ClearScrollResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final ClearScrollResponse clearScroll(ClearScrollRequest clearScrollRequest, Header... headers) throws IOException {
        return (ClearScrollResponse)this.performRequestAndParseEntity((ActionRequest)clearScrollRequest, RequestConverters::clearScroll, (CheckedFunction)(ClearScrollResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void clearScrollAsync(ClearScrollRequest clearScrollRequest, RequestOptions options, ActionListener<ClearScrollResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)clearScrollRequest, RequestConverters::clearScroll, (RequestOptions)options, (CheckedFunction)(ClearScrollResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void clearScrollAsync(ClearScrollRequest clearScrollRequest, ActionListener<ClearScrollResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)clearScrollRequest, RequestConverters::clearScroll, (CheckedFunction)(ClearScrollResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final SearchTemplateResponse searchTemplate(SearchTemplateRequest searchTemplateRequest, RequestOptions options) throws IOException {
        return (SearchTemplateResponse)this.performRequestAndParseEntity((ActionRequest)searchTemplateRequest, RequestConverters::searchTemplate, (RequestOptions)options, (CheckedFunction)(SearchTemplateResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void searchTemplateAsync(SearchTemplateRequest searchTemplateRequest, RequestOptions options, ActionListener<SearchTemplateResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)searchTemplateRequest, RequestConverters::searchTemplate, (RequestOptions)options, (CheckedFunction)(SearchTemplateResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final ExplainResponse explain(ExplainRequest explainRequest, RequestOptions options) throws IOException {
        return (ExplainResponse)this.performRequest((ActionRequest)explainRequest, RequestConverters::explain, (RequestOptions)options, (CheckedFunction)((response) -> {
            CheckedFunction<XContentParser, ExplainResponse, IOException> entityParser = (parser) -> {
                return ExplainResponse.fromXContent(parser, convertExistsResponse(response));
            };
            return (ExplainResponse)this.parseEntity(response.getEntity(), entityParser);
        }), (Set)Collections.singleton(404));
    }

    public final void explainAsync(ExplainRequest explainRequest, RequestOptions options, ActionListener<ExplainResponse> listener) {
        this.performRequestAsync((ActionRequest)explainRequest, RequestConverters::explain, (RequestOptions)options, (CheckedFunction)((response) -> {
            CheckedFunction<XContentParser, ExplainResponse, IOException> entityParser = (parser) -> {
                return ExplainResponse.fromXContent(parser, convertExistsResponse(response));
            };
            return (ExplainResponse)this.parseEntity(response.getEntity(), entityParser);
        }), (ActionListener)listener, (Set)Collections.singleton(404));
    }

    public final TermVectorsResponse termvectors(TermVectorsRequest request, RequestOptions options) throws IOException {
        return (TermVectorsResponse)this.performRequestAndParseEntity((Validatable)request, RequestConverters::termVectors, (RequestOptions)options, (CheckedFunction)(TermVectorsResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void termvectorsAsync(TermVectorsRequest request, RequestOptions options, ActionListener<TermVectorsResponse> listener) {
        this.performRequestAsyncAndParseEntity((Validatable)request, RequestConverters::termVectors, (RequestOptions)options, (CheckedFunction)(TermVectorsResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final MultiTermVectorsResponse mtermvectors(MultiTermVectorsRequest request, RequestOptions options) throws IOException {
        return (MultiTermVectorsResponse)this.performRequestAndParseEntity((Validatable)request, RequestConverters::mtermVectors, (RequestOptions)options, (CheckedFunction)(MultiTermVectorsResponse::fromXContent), (Set)Collections.emptySet());
    }

    public final void mtermvectorsAsync(MultiTermVectorsRequest request, RequestOptions options, ActionListener<MultiTermVectorsResponse> listener) {
        this.performRequestAsyncAndParseEntity((Validatable)request, RequestConverters::mtermVectors, (RequestOptions)options, (CheckedFunction)(MultiTermVectorsResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final RankEvalResponse rankEval(RankEvalRequest rankEvalRequest, RequestOptions options) throws IOException {
        return (RankEvalResponse)this.performRequestAndParseEntity((ActionRequest)rankEvalRequest, RequestConverters::rankEval, (RequestOptions)options, (CheckedFunction)(RankEvalResponse::fromXContent), (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final RankEvalResponse rankEval(RankEvalRequest rankEvalRequest, Header... headers) throws IOException {
        return (RankEvalResponse)this.performRequestAndParseEntity((ActionRequest)rankEvalRequest, RequestConverters::rankEval, (CheckedFunction)(RankEvalResponse::fromXContent), (Set)Collections.emptySet(), (Header[])headers);
    }

    public final void rankEvalAsync(RankEvalRequest rankEvalRequest, RequestOptions options, ActionListener<RankEvalResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)rankEvalRequest, RequestConverters::rankEval, (RequestOptions)options, (CheckedFunction)(RankEvalResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final MultiSearchTemplateResponse msearchTemplate(MultiSearchTemplateRequest multiSearchTemplateRequest, RequestOptions options) throws IOException {
        return (MultiSearchTemplateResponse)this.performRequestAndParseEntity((ActionRequest)multiSearchTemplateRequest, RequestConverters::multiSearchTemplate, (RequestOptions)options, (CheckedFunction)(MultiSearchTemplateResponse::fromXContext), (Set)Collections.emptySet());
    }

    public final void msearchTemplateAsync(MultiSearchTemplateRequest multiSearchTemplateRequest, RequestOptions options, ActionListener<MultiSearchTemplateResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)multiSearchTemplateRequest, RequestConverters::multiSearchTemplate, (RequestOptions)options, (CheckedFunction)(MultiSearchTemplateResponse::fromXContext), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    public final void rankEvalAsync(RankEvalRequest rankEvalRequest, ActionListener<RankEvalResponse> listener, Header... headers) {
        this.performRequestAsyncAndParseEntity((ActionRequest)rankEvalRequest, RequestConverters::rankEval, (CheckedFunction)(RankEvalResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet(), (Header[])headers);
    }

    public final FieldCapabilitiesResponse fieldCaps(FieldCapabilitiesRequest fieldCapabilitiesRequest, RequestOptions options) throws IOException {
        return (FieldCapabilitiesResponse)this.performRequestAndParseEntity((ActionRequest)fieldCapabilitiesRequest, RequestConverters::fieldCaps, (RequestOptions)options, (CheckedFunction)(FieldCapabilitiesResponse::fromXContent), (Set)Collections.emptySet());
    }

    public GetStoredScriptResponse getScript(GetStoredScriptRequest request, RequestOptions options) throws IOException {
        return (GetStoredScriptResponse)this.performRequestAndParseEntity((ActionRequest)request, RequestConverters::getScript, (RequestOptions)options, (CheckedFunction)(GetStoredScriptResponse::fromXContent), (Set)Collections.emptySet());
    }

    public void getScriptAsync(GetStoredScriptRequest request, RequestOptions options, ActionListener<GetStoredScriptResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)request, RequestConverters::getScript, (RequestOptions)options, (CheckedFunction)(GetStoredScriptResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public AcknowledgedResponse deleteScript(DeleteStoredScriptRequest request, RequestOptions options) throws IOException {
        return (AcknowledgedResponse)this.performRequestAndParseEntity((ActionRequest)request, RequestConverters::deleteScript, (RequestOptions)options, (CheckedFunction)(AcknowledgedResponse::fromXContent), (Set)Collections.emptySet());
    }

    public void deleteScriptAsync(DeleteStoredScriptRequest request, RequestOptions options, ActionListener<AcknowledgedResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)request, RequestConverters::deleteScript, (RequestOptions)options, (CheckedFunction)(AcknowledgedResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public AcknowledgedResponse putScript(PutStoredScriptRequest putStoredScriptRequest, RequestOptions options) throws IOException {
        return (AcknowledgedResponse)this.performRequestAndParseEntity((ActionRequest)putStoredScriptRequest, RequestConverters::putScript, (RequestOptions)options, (CheckedFunction)(AcknowledgedResponse::fromXContent), (Set)Collections.emptySet());
    }

    public void putScriptAsync(PutStoredScriptRequest putStoredScriptRequest, RequestOptions options, ActionListener<AcknowledgedResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)putStoredScriptRequest, RequestConverters::putScript, (RequestOptions)options, (CheckedFunction)(AcknowledgedResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    public final void fieldCapsAsync(FieldCapabilitiesRequest fieldCapabilitiesRequest, RequestOptions options, ActionListener<FieldCapabilitiesResponse> listener) {
        this.performRequestAsyncAndParseEntity((ActionRequest)fieldCapabilitiesRequest, RequestConverters::fieldCaps, (RequestOptions)options, (CheckedFunction)(FieldCapabilitiesResponse::fromXContent), (ActionListener)listener, (Set)Collections.emptySet());
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> Resp performRequestAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, CheckedFunction<XContentParser, Resp, IOException> entityParser, Set<Integer> ignores, Header... headers) throws IOException {
        return this.performRequest(request, requestConverter, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, ignores, headers);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> Resp performRequestAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser, Set<Integer> ignores) throws IOException {
        return this.performRequest(request, requestConverter, options, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, ignores);
    }

    protected final <Req extends Validatable, Resp> Resp performRequestAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser, Set<Integer> ignores) throws IOException {
        return this.performRequest(request, requestConverter, options, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, ignores);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> Resp performRequest(Req request, CheckedFunction<Req, Request, IOException> requestConverter, CheckedFunction<Response, Resp, IOException> responseConverter, Set<Integer> ignores, Header... headers) throws IOException {
        return this.performRequest(request, requestConverter, optionsForHeaders(headers), responseConverter, ignores);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> Resp performRequest(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, Set<Integer> ignores) throws IOException {
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null && !validationException.validationErrors().isEmpty()) {
            throw validationException;
        } else {
            return this.internalPerformRequest(request, requestConverter, options, responseConverter, ignores);
        }
    }

    protected final <Req extends Validatable, Resp> Resp performRequest(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, Set<Integer> ignores) throws IOException {
        Optional<ValidationException> validationException = request.validate();
        if (validationException != null && validationException.isPresent()) {
            throw (ValidationException)validationException.get();
        } else {
            return this.internalPerformRequest(request, requestConverter, options, responseConverter, ignores);
        }
    }

    public <Req, Resp> Resp internalPerformRequest(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, Set<Integer> ignores) throws IOException {
        Request req = (Request)requestConverter.apply(request);
        req.setOptions(options);

        Response response;
        try {
            response = this.client.performRequest(req);
        } catch (ResponseException var12) {
            ResponseException e = var12;
            if (ignores.contains(var12.getResponse().getStatusLine().getStatusCode())) {
                try {
                    return responseConverter.apply(e.getResponse());
                } catch (Exception var10) {
                    throw this.parseResponseException(var12);
                }
            }

            throw this.parseResponseException(var12);
        }

        try {
            return responseConverter.apply(response);
        } catch (Exception var11) {
            throw new IOException("Unable to parse response body for " + response, var11);
        }
    }

    protected final <Req extends Validatable, Resp> Optional<Resp> performRequestAndParseOptionalEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser) throws IOException {
        Optional<ValidationException> validationException = request.validate();
        if (validationException != null && validationException.isPresent()) {
            throw (ValidationException)validationException.get();
        } else {
            Request req = (Request)requestConverter.apply(request);
            req.setOptions(options);

            Response response;
            try {
                response = this.client.performRequest(req);
            } catch (ResponseException var10) {
                if (RestStatus.NOT_FOUND.getStatus() == var10.getResponse().getStatusLine().getStatusCode()) {
                    return Optional.empty();
                }

                throw this.parseResponseException(var10);
            }

            try {
                return Optional.of(this.parseEntity(response.getEntity(), entityParser));
            } catch (Exception var9) {
                throw new IOException("Unable to parse response body for " + response, var9);
            }
        }
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> void performRequestAsyncAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, CheckedFunction<XContentParser, Resp, IOException> entityParser, ActionListener<Resp> listener, Set<Integer> ignores, Header... headers) {
        this.performRequestAsync(request, requestConverter, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, listener, ignores, headers);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> void performRequestAsyncAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser, ActionListener<Resp> listener, Set<Integer> ignores) {
        this.performRequestAsync(request, requestConverter, options, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, listener, ignores);
    }

    protected final <Req extends Validatable, Resp> void performRequestAsyncAndParseEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser, ActionListener<Resp> listener, Set<Integer> ignores) {
        this.performRequestAsync(request, requestConverter, options, (response) -> {
            return this.parseEntity(response.getEntity(), entityParser);
        }, listener, ignores);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> void performRequestAsync(Req request, CheckedFunction<Req, Request, IOException> requestConverter, CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Resp> listener, Set<Integer> ignores, Header... headers) {
        this.performRequestAsync(request, requestConverter, optionsForHeaders(headers), responseConverter, listener, ignores);
    }

    /** @deprecated */
    @Deprecated
    protected final <Req extends ActionRequest, Resp> void performRequestAsync(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Resp> listener, Set<Integer> ignores) {
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null && !validationException.validationErrors().isEmpty()) {
            listener.onFailure(validationException);
        } else {
            this.internalPerformRequestAsync(request, requestConverter, options, responseConverter, listener, ignores);
        }
    }

    protected final <Req extends Validatable, Resp> void performRequestAsync(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Resp> listener, Set<Integer> ignores) {
        Optional<ValidationException> validationException = request.validate();
        if (validationException != null && validationException.isPresent()) {
            listener.onFailure((Exception)validationException.get());
        } else {
            this.internalPerformRequestAsync(request, requestConverter, options, responseConverter, listener, ignores);
        }
    }

    private <Req, Resp> void internalPerformRequestAsync(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Resp> listener, Set<Integer> ignores) {
        Request req;
        try {
            req = (Request)requestConverter.apply(request);
        } catch (Exception var9) {
            listener.onFailure(var9);
            return;
        }

        req.setOptions(options);
        ResponseListener responseListener = this.wrapResponseListener(responseConverter, listener, ignores);
        this.client.performRequestAsync(req, responseListener);
    }

    final <Resp> ResponseListener wrapResponseListener(CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Resp> actionListener, Set<Integer> ignores) {
        return new ResponseListener() {
            public void onSuccess(Response response) {
                try {
                    actionListener.onResponse(responseConverter.apply(response));
                } catch (Exception var4) {
                    IOException ioe = new IOException("Unable to parse response body for " + response, var4);
                    this.onFailure(ioe);
                }

            }

            public void onFailure(Exception exception) {
                if (exception instanceof ResponseException) {
                    ResponseException responseException = (ResponseException)exception;
                    Response response = responseException.getResponse();
                    if (ignores.contains(response.getStatusLine().getStatusCode())) {
                        try {
                            actionListener.onResponse(responseConverter.apply(response));
                        } catch (Exception var5) {
                            actionListener.onFailure(RestHighLevelClient.this.parseResponseException(responseException));
                        }
                    } else {
                        actionListener.onFailure(RestHighLevelClient.this.parseResponseException(responseException));
                    }
                } else {
                    actionListener.onFailure(exception);
                }

            }
        };
    }

    protected final <Req extends Validatable, Resp> void performRequestAsyncAndParseOptionalEntity(Req request, CheckedFunction<Req, Request, IOException> requestConverter, RequestOptions options, CheckedFunction<XContentParser, Resp, IOException> entityParser, ActionListener<Optional<Resp>> listener) {
        Optional<ValidationException> validationException = request.validate();
        if (validationException != null && validationException.isPresent()) {
            listener.onFailure((Exception)validationException.get());
        } else {
            Request req;
            try {
                req = (Request)requestConverter.apply(request);
            } catch (Exception var9) {
                listener.onFailure(var9);
                return;
            }

            req.setOptions(options);
            ResponseListener responseListener = this.wrapResponseListener404sOptional((response) -> {
                return this.parseEntity(response.getEntity(), entityParser);
            }, listener);
            this.client.performRequestAsync(req, responseListener);
        }
    }

    final <Resp> ResponseListener wrapResponseListener404sOptional(CheckedFunction<Response, Resp, IOException> responseConverter, ActionListener<Optional<Resp>> actionListener) {
        return new ResponseListener() {
            public void onSuccess(Response response) {
                try {
                    actionListener.onResponse(Optional.of(responseConverter.apply(response)));
                } catch (Exception var4) {
                    IOException ioe = new IOException("Unable to parse response body for " + response, var4);
                    this.onFailure(ioe);
                }

            }

            public void onFailure(Exception exception) {
                if (exception instanceof ResponseException) {
                    ResponseException responseException = (ResponseException)exception;
                    Response response = responseException.getResponse();
                    if (RestStatus.NOT_FOUND.getStatus() == response.getStatusLine().getStatusCode()) {
                        actionListener.onResponse(Optional.empty());
                    } else {
                        actionListener.onFailure(RestHighLevelClient.this.parseResponseException(responseException));
                    }
                } else {
                    actionListener.onFailure(exception);
                }

            }
        };
    }

    protected final ElasticsearchStatusException parseResponseException(ResponseException responseException) {
        Response response = responseException.getResponse();
        HttpEntity entity = response.getEntity();
        RestStatus restStatus = RestStatus.fromCode(response.getStatusLine().getStatusCode());
        ElasticsearchStatusException elasticsearchException;
        if (entity == null) {
            elasticsearchException = new ElasticsearchStatusException(responseException.getMessage(), restStatus, responseException, new Object[0]);
        } else {
            try {
                elasticsearchException = (ElasticsearchStatusException)this.parseEntity(entity, BytesRestResponse::errorFromXContent);
                elasticsearchException.addSuppressed(responseException);
            } catch (Exception var7) {
                elasticsearchException = new ElasticsearchStatusException("Unable to parse response body", restStatus, responseException, new Object[0]);
                elasticsearchException.addSuppressed(var7);
            }
        }

        return elasticsearchException;
    }

    protected final <Resp> Resp parseEntity(final HttpEntity entity, final CheckedFunction<XContentParser, Resp, IOException> entityParser) throws IOException {
        if (entity == null) {
            throw new IllegalStateException("Response body expected but not returned");
        } else if (entity.getContentType() == null) {
            throw new IllegalStateException("Elasticsearch didn't return the [Content-Type] header, unable to parse response body");
        } else {
            XContentType xContentType = XContentType.fromMediaTypeOrFormat(entity.getContentType().getValue());
            if (xContentType == null) {
                throw new IllegalStateException("Unsupported Content-Type: " + entity.getContentType().getValue());
            } else {
                XContentParser parser = xContentType.xContent().createParser(this.registry, DEPRECATION_HANDLER, entity.getContent());
                Throwable var5 = null;

                Object var6;
                try {
                    var6 = entityParser.apply(parser);
                } catch (Throwable var15) {
                    var5 = var15;
                    throw var15;
                } finally {
                    if (parser != null) {
                        if (var5 != null) {
                            try {
                                parser.close();
                            } catch (Throwable var14) {
                                var5.addSuppressed(var14);
                            }
                        } else {
                            parser.close();
                        }
                    }

                }

                return var6;
            }
        }
    }

    private static RequestOptions optionsForHeaders(Header[] headers) {
        Builder options = RequestOptions.DEFAULT.toBuilder();
        Header[] var2 = headers;
        int var3 = headers.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Header header = var2[var4];
            Objects.requireNonNull(header, "header cannot be null");
            options.addHeader(header.getName(), header.getValue());
        }

        return options.build();
    }

    protected static boolean convertExistsResponse(Response response) {
        return response.getStatusLine().getStatusCode() == 200;
    }

    static List<Entry> getDefaultNamedXContents() {
        Map<String, ContextParser<Object, ? extends Aggregation>> map = new HashMap();
        map.put("cardinality", (p, c) -> {
            return ParsedCardinality.fromXContent(p, (String)c);
        });
        map.put("hdr_percentiles", (p, c) -> {
            return ParsedHDRPercentiles.fromXContent(p, (String)c);
        });
        map.put("hdr_percentile_ranks", (p, c) -> {
            return ParsedHDRPercentileRanks.fromXContent(p, (String)c);
        });
        map.put("tdigest_percentiles", (p, c) -> {
            return ParsedTDigestPercentiles.fromXContent(p, (String)c);
        });
        map.put("tdigest_percentile_ranks", (p, c) -> {
            return ParsedTDigestPercentileRanks.fromXContent(p, (String)c);
        });
        map.put("percentiles_bucket", (p, c) -> {
            return ParsedPercentilesBucket.fromXContent(p, (String)c);
        });
        map.put("median_absolute_deviation", (p, c) -> {
            return ParsedMedianAbsoluteDeviation.fromXContent(p, (String)c);
        });
        map.put("min", (p, c) -> {
            return ParsedMin.fromXContent(p, (String)c);
        });
        map.put("max", (p, c) -> {
            return ParsedMax.fromXContent(p, (String)c);
        });
        map.put("sum", (p, c) -> {
            return ParsedSum.fromXContent(p, (String)c);
        });
        map.put("avg", (p, c) -> {
            return ParsedAvg.fromXContent(p, (String)c);
        });
        map.put("weighted_avg", (p, c) -> {
            return ParsedWeightedAvg.fromXContent(p, (String)c);
        });
        map.put("value_count", (p, c) -> {
            return ParsedValueCount.fromXContent(p, (String)c);
        });
        map.put("simple_value", (p, c) -> {
            return ParsedSimpleValue.fromXContent(p, (String)c);
        });
        map.put("derivative", (p, c) -> {
            return ParsedDerivative.fromXContent(p, (String)c);
        });
        map.put("bucket_metric_value", (p, c) -> {
            return ParsedBucketMetricValue.fromXContent(p, (String)c);
        });
        map.put("stats", (p, c) -> {
            return ParsedStats.fromXContent(p, (String)c);
        });
        map.put("stats_bucket", (p, c) -> {
            return ParsedStatsBucket.fromXContent(p, (String)c);
        });
        map.put("extended_stats", (p, c) -> {
            return ParsedExtendedStats.fromXContent(p, (String)c);
        });
        map.put("extended_stats_bucket", (p, c) -> {
            return ParsedExtendedStatsBucket.fromXContent(p, (String)c);
        });
        map.put("geo_bounds", (p, c) -> {
            return ParsedGeoBounds.fromXContent(p, (String)c);
        });
        map.put("geo_centroid", (p, c) -> {
            return ParsedGeoCentroid.fromXContent(p, (String)c);
        });
        map.put("histogram", (p, c) -> {
            return ParsedHistogram.fromXContent(p, (String)c);
        });
        map.put("date_histogram", (p, c) -> {
            return ParsedDateHistogram.fromXContent(p, (String)c);
        });
        map.put("auto_date_histogram", (p, c) -> {
            return ParsedAutoDateHistogram.fromXContent(p, (String)c);
        });
        map.put("sterms", (p, c) -> {
            return ParsedStringTerms.fromXContent(p, (String)c);
        });
        map.put("lterms", (p, c) -> {
            return ParsedLongTerms.fromXContent(p, (String)c);
        });
        map.put("dterms", (p, c) -> {
            return ParsedDoubleTerms.fromXContent(p, (String)c);
        });
        map.put("missing", (p, c) -> {
            return ParsedMissing.fromXContent(p, (String)c);
        });
        map.put("nested", (p, c) -> {
            return ParsedNested.fromXContent(p, (String)c);
        });
        map.put("reverse_nested", (p, c) -> {
            return ParsedReverseNested.fromXContent(p, (String)c);
        });
        map.put("global", (p, c) -> {
            return ParsedGlobal.fromXContent(p, (String)c);
        });
        map.put("filter", (p, c) -> {
            return ParsedFilter.fromXContent(p, (String)c);
        });
        map.put("sampler", (p, c) -> {
            return ParsedSampler.fromXContent(p, (String)c);
        });
        map.put("geohash_grid", (p, c) -> {
            return ParsedGeoHashGrid.fromXContent(p, (String)c);
        });
        map.put("range", (p, c) -> {
            return ParsedRange.fromXContent(p, (String)c);
        });
        map.put("date_range", (p, c) -> {
            return ParsedDateRange.fromXContent(p, (String)c);
        });
        map.put("geo_distance", (p, c) -> {
            return ParsedGeoDistance.fromXContent(p, (String)c);
        });
        map.put("filters", (p, c) -> {
            return ParsedFilters.fromXContent(p, (String)c);
        });
        map.put("adjacency_matrix", (p, c) -> {
            return ParsedAdjacencyMatrix.fromXContent(p, (String)c);
        });
        map.put("siglterms", (p, c) -> {
            return ParsedSignificantLongTerms.fromXContent(p, (String)c);
        });
        map.put("sigsterms", (p, c) -> {
            return ParsedSignificantStringTerms.fromXContent(p, (String)c);
        });
        map.put("scripted_metric", (p, c) -> {
            return ParsedScriptedMetric.fromXContent(p, (String)c);
        });
        map.put("ip_range", (p, c) -> {
            return ParsedBinaryRange.fromXContent(p, (String)c);
        });
        map.put("top_hits", (p, c) -> {
            return ParsedTopHits.fromXContent(p, (String)c);
        });
        map.put("composite", (p, c) -> {
            return ParsedComposite.fromXContent(p, (String)c);
        });
        List<Entry> entries = (List)map.entrySet().stream().map((entry) -> {
            return new Entry(Aggregation.class, new ParseField((String)entry.getKey(), new String[0]), (ContextParser)entry.getValue());
        }).collect(Collectors.toList());
        entries.add(new Entry(Suggestion.class, new ParseField("term", new String[0]), (parser, context) -> {
            return TermSuggestion.fromXContent(parser, (String)context);
        }));
        entries.add(new Entry(Suggestion.class, new ParseField("phrase", new String[0]), (parser, context) -> {
            return PhraseSuggestion.fromXContent(parser, (String)context);
        }));
        entries.add(new Entry(Suggestion.class, new ParseField("completion", new String[0]), (parser, context) -> {
            return CompletionSuggestion.fromXContent(parser, (String)context);
        }));
        return entries;
    }

    static List<Entry> getProvidedNamedXContents() {
        List<Entry> entries = new ArrayList();
        Iterator var1 = ServiceLoader.load(NamedXContentProvider.class).iterator();

        while(var1.hasNext()) {
            NamedXContentProvider service = (NamedXContentProvider)var1.next();
            entries.addAll(service.getNamedXContentParsers());
        }

        return entries;
    }
}

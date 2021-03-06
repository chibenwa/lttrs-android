/*
 * Copyright 2019 Daniel Gultsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rs.ltt.android.ui.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;
import androidx.work.WorkManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.concurrent.Future;

import rs.ltt.android.entity.AccountWithCredentials;
import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.ThreadOverviewItem;
import rs.ltt.android.repository.QueryRepository;
import rs.ltt.android.util.WorkInfoUtil;
import rs.ltt.android.worker.AbstractMuaWorker;
import rs.ltt.jmap.common.entity.Keyword;
import rs.ltt.jmap.common.entity.query.EmailQuery;

public abstract class AbstractQueryViewModel extends AndroidViewModel {

    final QueryRepository queryRepository;
    private final LiveData<Boolean> emailModificationWorkInfo;
    private LiveData<PagedList<ThreadOverviewItem>> threads;
    private LiveData<Boolean> refreshing;
    private LiveData<Boolean> runningPagingRequest;
    private ListenableFuture<MailboxWithRoleAndName> important;

    AbstractQueryViewModel(@NonNull Application application, final long accountId) {
        super(application);

        final WorkManager workManager = WorkManager.getInstance(application);

        this.queryRepository = new QueryRepository(application, accountId);
        this.important = this.queryRepository.getImportant();
        this.emailModificationWorkInfo = Transformations.map(workManager.getWorkInfosByTagLiveData(AbstractMuaWorker.TAG_EMAIL_MODIFICATION), WorkInfoUtil::allDone);
    }

    void init() {
        this.threads = Transformations.switchMap(getQuery(), queryRepository::getThreadOverviewItems);
        this.refreshing = Transformations.switchMap(getQuery(), queryRepository::isRunningQueryFor);
        this.runningPagingRequest = Transformations.switchMap(getQuery(), queryRepository::isRunningPagingRequestFor);
    }

    public LiveData<Boolean> isRefreshing() {
        final LiveData<Boolean> refreshing = this.refreshing;
        if (refreshing == null) {
            throw new IllegalStateException("LiveData for refreshing not initialized. Forgot to call init()?");
        }
        return refreshing;
    }

    public Future<MailboxWithRoleAndName> getImportant() {
        return this.important;
    }

    public LiveData<Boolean> isRunningPagingRequest() {
        final LiveData<Boolean> paging = this.runningPagingRequest;
        if (paging == null) {
            throw new IllegalStateException("LiveData for paging not initialized. Forgot to call init()?");
        }
        return paging;
    }

    public LiveData<PagedList<ThreadOverviewItem>> getThreadOverviewItems() {
        final LiveData<PagedList<ThreadOverviewItem>> liveData = this.threads;
        if (liveData == null) {
            throw new IllegalStateException("LiveData for thread items not initialized. Forgot to call init()?");
        }
        return liveData;
    }

    public LiveData<Boolean> getEmailModificationWorkInfo() {
        return emailModificationWorkInfo;
    }

    public void onRefresh() {
        final EmailQuery emailQuery = getQuery().getValue();
        if (emailQuery != null) {
            queryRepository.refresh(emailQuery);
        }
    }

    public void refreshInBackground() {
        final EmailQuery emailQuery = getQuery().getValue();
        if (emailQuery != null) {
            queryRepository.refreshInBackground(emailQuery);
        }
    }


    protected abstract LiveData<EmailQuery> getQuery();

    public void toggleFlagged(String threadId, boolean target) {
        this.queryRepository.toggleFlagged(ImmutableSet.of(threadId), target);
    }

    public void markRead(final Collection<String> threadIds) {
        queryRepository.markRead(threadIds);
    }

    public void markUnread(final Collection<String> threadIds) {
        queryRepository.markUnRead(threadIds);
    }

    public void markImportant(final Collection<String> threadIds) {
        queryRepository.markImportant(threadIds);
    }

    public void markNotImportant(final Collection<String> threadIds) {
        queryRepository.markNotImportant(threadIds);
    }

    public void addFlag(final Collection<String> threadIds) {
        queryRepository.addKeyword(threadIds, Keyword.FLAGGED);
    }

    public void removeFlag(final Collection<String> threadIds) {
        queryRepository.removeKeyword(threadIds, Keyword.FLAGGED);
    }


}

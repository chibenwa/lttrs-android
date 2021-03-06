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

package rs.ltt.android.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;

import rs.ltt.android.entity.AccountWithCredentials;
import rs.ltt.android.entity.ExpandedPosition;
import rs.ltt.android.entity.FullEmail;
import rs.ltt.android.entity.KeywordOverwriteEntity;
import rs.ltt.android.entity.MailboxOverwriteEntity;
import rs.ltt.android.entity.MailboxWithRoleAndName;
import rs.ltt.android.entity.ThreadHeader;

public class ThreadRepository extends LttrsRepository {

    public ThreadRepository(final Application application, final long accountId) {
        super(application, accountId);
    }

    public LiveData<PagedList<FullEmail>> getEmails(String threadId) {
        return new LivePagedListBuilder<>(database.threadAndEmailDao().getEmails(threadId), 30).build();
    }

    public LiveData<ThreadHeader> getThreadHeader(String threadId) {
        return database.threadAndEmailDao().getThreadHeader(threadId);
    }

    public LiveData<List<MailboxWithRoleAndName>> getMailboxes(String threadId) {
        return database.mailboxDao().getMailboxesForThreadLiveData(threadId);
    }


    public LiveData<List<MailboxOverwriteEntity>> getMailboxOverwrites(String threadId) {
        return database.overwriteDao().getMailboxOverwrites(threadId);
    }

    public ListenableFuture<List<ExpandedPosition>> getExpandedPositions(String threadId) {
        ListenableFuture<KeywordOverwriteEntity> overwriteFuture = database.overwriteDao().getKeywordOverwrite(threadId);
        return Futures.transformAsync(overwriteFuture, overwrite -> {
            if (overwrite != null) {
                if (overwrite.value) {
                    return database.threadAndEmailDao().getMaxPosition(threadId);
                } else {
                    return database.threadAndEmailDao().getAllPositions(threadId);
                }
            } else {
                ListenableFuture<List<ExpandedPosition>> unseenFuture = database.threadAndEmailDao().getUnseenPositions(threadId);
                return Futures.transformAsync(unseenFuture, unseen -> {
                    if (unseen == null || unseen.size() == 0) {
                        return database.threadAndEmailDao().getMaxPosition(threadId);
                    } else {
                        return Futures.immediateFuture(unseen);
                    }
                }, MoreExecutors.directExecutor());
            }
        }, MoreExecutors.directExecutor());
    }
}

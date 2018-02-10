/*
 * Copyright (c) 2015 PocketHub
 *
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
package com.github.pockethub.android.tests;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.ui.item.news.NewsItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.ReferenceType;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.Team;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload;
import com.meisolsson.githubsdk.model.payload.CreatePayload;
import com.meisolsson.githubsdk.model.payload.DeletePayload;
import com.meisolsson.githubsdk.model.payload.FollowPayload;
import com.meisolsson.githubsdk.model.payload.GistPayload;
import com.meisolsson.githubsdk.model.payload.GitHubPayload;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;
import com.meisolsson.githubsdk.model.payload.MemberPayload;
import com.meisolsson.githubsdk.model.payload.PullRequestPayload;
import com.meisolsson.githubsdk.model.payload.PushPayload;
import com.meisolsson.githubsdk.model.payload.TeamAddPayload;

import java.util.Collections;
import java.util.Date;

/**
 * Tests of the news text rendering
 */
public class NewsEventTextTest extends InstrumentationTestCase {

    private TextView text;

    private User actor;

    private GitHubEvent.RepoIdentifier repo;

    private AvatarLoader avatarLoader;

    private LayoutInflater layoutInflater;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        actor = User.builder().login("user").build();
        repo = new GitHubEvent.RepoIdentifier() {
            @Nullable
            @Override
            public Long id() {
                return null;
            }

            @Nullable
            @Override
            public String url() {
                return null;
            }

            @Nullable
            @Override
            public String repoWithUserName() {
                return "user/repo";
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {

            }
        };
        Context context = getInstrumentation().getTargetContext();
        avatarLoader = new AvatarLoader(context);
        layoutInflater = LayoutInflater.from(context);
    }

    private GitHubEvent createEvent(GitHubEventType type, GitHubPayload payload) {
        return GitHubEvent.builder()
                .id("test")
                .createdAt(new Date())
                .type(type)
                .payload(payload)
                .actor(actor)
                .repo(repo)
                .build();
    }

    private void verify(String expected) {
        CharSequence actual = text.getText();
        assertNotNull(actual);
        assertEquals(expected, actual.toString());
    }

    private void updateView(GitHubEvent event) {
        NewsItem item = NewsItem.createNewsItem(avatarLoader, event);

        View itemView = layoutInflater.inflate(item.getLayout(), null);
        NewsItem.ViewHolder viewHolder = item.createViewHolder(itemView);
        item.bind(viewHolder, 0);

        text = viewHolder.event;
        assertNotNull(text);
    }

    /**
     * Verify text of commit comment event
     */
    @UiThreadTest
    public void testCommitCommentEvent() {
        GitHubEvent event = createEvent(GitHubEventType.CommitCommentEvent,
                CommitCommentPayload.builder().build());
        updateView(event);

        verify("user commented on user/repo");
    }

    /**
     * Verify text of create event
     */
    @UiThreadTest
    public void testCreateRepositoryEvent() {
        CreatePayload payload = CreatePayload.builder()
                .refType(ReferenceType.Repository)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.CreateEvent, payload);
        updateView(event);

        verify("user created repository repo");
    }

    /**
     * Verify text of create event
     */
    @UiThreadTest
    public void testCreateBranchEvent() {
        CreatePayload payload = CreatePayload.builder()
                .refType(ReferenceType.Branch)
                .ref("b1")
                .build();

        GitHubEvent event = createEvent(GitHubEventType.CreateEvent, payload);
        updateView(event);

        verify("user created branch b1 at user/repo");
    }

    /**
     * Verify text of delete event
     */
    @UiThreadTest
    public void testDelete() {
        DeletePayload payload = DeletePayload.builder()
                .refType(ReferenceType.Branch)
                .ref("b1")
                .build();

        GitHubEvent event = createEvent(GitHubEventType.DeleteEvent, payload);
        updateView(event);

        verify("user deleted branch b1 at user/repo");
    }

    /**
     * Verify text of follow event
     */
    @UiThreadTest
    public void testFollow() {
        User target = User.builder()
                .login("user2")
                .build();

        FollowPayload payload = FollowPayload.builder()
                .target(target)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.FollowEvent, payload);
        updateView(event);

        verify("user started following user2");
    }

    /**
     * Verify text of Gist event
     */
    @UiThreadTest
    public void testGist() {
        Gist gist = Gist.builder()
                .id("1")
                .build();

        GistPayload payload = GistPayload.builder()
                .action(GistPayload.Action.Created)
                .gist(gist)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.GistEvent, payload);
        updateView(event);

        verify("user created Gist 1");
    }

    /**
     * Verify text of wiki event
     */
    @UiThreadTest
    public void testWiki() {
        GitHubEvent event = createEvent(GitHubEventType.GollumEvent, null);
        updateView(event);

        verify("user updated the wiki in user/repo");
    }

    /**
     * Verify text of issue comment event
     */
    @UiThreadTest
    public void testIssueComment() {
        Issue issue = Issue.builder()
                .number(5)
                .build();

        IssueCommentPayload payload = IssueCommentPayload.builder()
                .issue(issue)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.IssueCommentEvent, payload);
        updateView(event);

        verify("user commented on issue 5 on user/repo");
    }

    /**
     * Verify text of issue event
     */
    @UiThreadTest
    public void testIssue() {
        Issue issue = Issue.builder()
                .number(8)
                .build();

        IssuesPayload payload = IssuesPayload.builder()
                .action(IssuesPayload.Action.Closed)
                .issue(issue)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.IssuesEvent, payload);
        updateView(event);

        verify("user closed issue 8 on user/repo");
    }

    /**
     * Verify text of member event
     */
    @UiThreadTest
    public void testAddMember() {
        User user = User.builder()
                .login("person")
                .build();

        MemberPayload payload = MemberPayload.builder()
                .member(user)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.MemberEvent, payload);
        updateView(event);

        verify("user added person as a collaborator to user/repo");
    }

    /**
     * Verify text of open sourced event
     */
    @UiThreadTest
    public void testOpenSourced() {
        GitHubEvent event = createEvent(GitHubEventType.PublicEvent, null);
        updateView(event);

        verify("user open sourced repository user/repo");
    }

    /**
     * Verify text of watch event
     */
    @UiThreadTest
    public void testWatch() {
        GitHubEvent event = createEvent(GitHubEventType.WatchEvent, null);
        updateView(event);

        verify("user starred user/repo");
    }

    /**
     * Verify text of pull request event
     */
    @UiThreadTest
    public void testPullRequest() {
        PullRequestPayload payload = PullRequestPayload.builder()
                .number(30)
                .action(PullRequestPayload.Action.Closed)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.PullRequestEvent, payload);
        updateView(event);

        verify("user closed pull request 30 on user/repo");
    }

    /**
     * Verify text of push event
     */
    @UiThreadTest
    public void testPush() {
        PushPayload payload = PushPayload.builder()
                .ref("refs/heads/master")
                .commits(Collections.emptyList())
                .build();

        GitHubEvent event = createEvent(GitHubEventType.PushEvent, payload);
        updateView(event);

        verify("user pushed to master at user/repo");
    }

    /**
     * Verify text of push event
     */
    @UiThreadTest
    public void testTeamAdd() {
        Team team = Team.builder()
                .name("t1")
                .build();

        Repository repo = Repository.builder()
                .name("r2")
                .build();

        TeamAddPayload payload = TeamAddPayload.builder()
                .repository(repo)
                .team(team)
                .build();

        GitHubEvent event = createEvent(GitHubEventType.TeamAddEvent, payload);
        updateView(event);

        verify("user added r2 to team t1");
    }
}
